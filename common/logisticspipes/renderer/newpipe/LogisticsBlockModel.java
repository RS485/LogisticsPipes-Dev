package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.vecmath.Matrix4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.textures.Textures;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsBlockModel implements IModel {

	interface BlockTypeProvider {
		LogisticsSolidBlock.BlockType getType();
	}

	public static class LogisticsBlockModelLoader implements ICustomModelLoader {

		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			if (modelLocation.getResourceDomain().equals("logisticspipes")) {
				if(modelLocation instanceof ModelResourceLocation) {
					if(((ModelResourceLocation)modelLocation).getVariant().equals("inventory")) {
						return LogisticsBlockModel.nameTextureIdMap.containsKey(modelLocation);
					}
					if(modelLocation.getResourcePath().equals("tile.logisticssolidblock")) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation) {
			if (modelLocation.getResourceDomain().equals("logisticspipes")) {
				if(modelLocation instanceof ModelResourceLocation) {
					if(((ModelResourceLocation)modelLocation).getVariant().equals("inventory")) {
						return new LogisticsBlockModel(() -> nameTextureIdMap.get(modelLocation));
					}
					if(modelLocation.getResourcePath().equals("tile.logisticssolidblock")) {
						String key = ((ModelResourceLocation) modelLocation).getVariant();
						key = key.substring(key.indexOf("block_sub_type=") + 15);
						key = key.substring(0, key.indexOf(","));
						LogisticsSolidBlock.BlockType type = LogisticsSolidBlock.BlockType.getForName(key);
						return new LogisticsBlockModel(() -> type);
					}
				}
			}
			return null;
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {

		}
	}

	public static Map<ModelResourceLocation, LogisticsSolidBlock.BlockType> nameTextureIdMap = Maps.newLinkedHashMap();

	private BlockTypeProvider typeProvider;

	public LogisticsBlockModel(BlockTypeProvider typeProvider) {
		this.typeProvider = typeProvider;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Lists.newArrayList();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Lists.newArrayList();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		final List<BakedQuad> quads = Lists.newArrayList();
		return new IBakedModel() {

			@Override
			@SideOnly(Side.CLIENT)
			public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
				if(side == null) {
					if(quads.isEmpty()) {
						quads.addAll(LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generateBlockRenderList(state), format));
					}
					return quads;
				} else {
					return Collections.EMPTY_LIST;//LogisticsRenderPipe.secondRenderer.getQuadsFromRenderList(generateBlockRenderList(state), format);
				}
			}

			@Override
			public boolean isAmbientOcclusion() {
				return false;
			}

			@Override
			public boolean isGui3d() {
				return true;
			}

			@Override
			public boolean isBuiltInRenderer() {
				return false;
			}

			@Override
			public TextureAtlasSprite getParticleTexture() {
				LogisticsSolidBlock.BlockType type = typeProvider.getType();
				if(type == null) {
					return null;
				}
				return LogisticsSolidBlock.getNewIcon(type);
			}

			@Override
			public ItemOverrideList getOverrides() {
				return ItemOverrideList.NONE;
			}

			@Override
			public org.apache.commons.lang3.tuple.Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
				return PerspectiveMapWrapper.handlePerspective(this, SimpleServiceLocator.cclProxy.getDefaultBlockState(), cameraTransformType);
			}
		};
	}

	private List<RenderEntry> generateBlockRenderList(@Nullable IBlockState state) {
		List<RenderEntry> objectsToRender = new ArrayList<>();

		LogisticsNewSolidBlockWorldRenderer.BlockRotation rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.ZERO;
		TextureTransformation icon;
		if(state != null) {
			rotation = LogisticsNewSolidBlockWorldRenderer.BlockRotation.getRotation(state.getValue(LogisticsSolidBlock.rotationProperty));
			icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(state.getValue(LogisticsSolidBlock.textureIndexProperty)));
		} else {
			icon = SimpleServiceLocator.cclProxy.createIconTransformer(LogisticsSolidBlock.getNewIcon(typeProvider.getType()));
		}


		//Draw
		objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.block.get(rotation), icon));
		for (LogisticsNewSolidBlockWorldRenderer.CoverSides side : LogisticsNewSolidBlockWorldRenderer.CoverSides.values()) {
			boolean render = true;
			if(state != null) {
				if(!state.getValue(LogisticsSolidBlock.connectionPropertys.get(side.getDir(rotation)))) {
					render = false;
				}
			}
			if (render) {
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Outer.get(side).get(rotation), icon));
				objectsToRender.add(new RenderEntry(LogisticsNewSolidBlockWorldRenderer.texturePlate_Inner.get(side).get(rotation), icon));
			}
		}

		return objectsToRender;
	}



}
