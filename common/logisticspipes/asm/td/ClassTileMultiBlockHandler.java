package logisticspipes.asm.td;

import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClassTileMultiBlockHandler {

	public static byte[] handleTileMultiBlockClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		String sumHandleEvent1 = ASMHelper.getCheckSumForMethod(reader, "getAdjTileEntitySafe", "(I)Lnet/minecraft/tileentity/TileEntity;");
		String sumHandleEvent2 = ASMHelper.getCheckSumForMethod(reader, "getConnectedSide", "(B)Lcofh/thermaldynamics/multiblock/IMultiBlock;");
		if(!"81F531AAAC7AC9CD38CDB49C8EDA994E13A1115A".equals(sumHandleEvent1)) noChecksumMatch = true;
		if(!"4C27D2EFAA3F5647ED61D72510E959DE9AFBB98E".equals(sumHandleEvent2)) noChecksumMatch = true;
		if(noChecksumMatch) {
			System.out.println("getAdjTileEntitySafe: " + sumHandleEvent1);
			System.out.println("getConnectedSide: " + sumHandleEvent2);
			new UnsupportedOperationException("This LP version isn't compatible with the installed TD version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}
		
		for(FieldNode f:node.fields) {
			if(f.name.equals("duct")) {
				f.access |= Opcodes.ACC_PUBLIC;
			}
		}
		
		for(MethodNode m:node.methods) {
			if(m.name.equals("getAdjTileEntitySafe") && m.desc.equals("(I)Lnet/minecraft/tileentity/TileEntity;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.ARETURN) {
							this.visitVarInsn(Opcodes.ILOAD, 1);
							this.visitVarInsn(Opcodes.ALOAD, 0);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "checkGetTileEntity", "(Lnet/minecraft/tileentity/TileEntity;ILcofh/thermaldynamics/block/TileMultiBlock;)Lnet/minecraft/tileentity/TileEntity;", false);
						}
						super.visitInsn(opcode);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("getConnectedSide") && m.desc.equals("(B)Lcofh/thermaldynamics/multiblock/IMultiBlock;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitTypeInsn(int opcode, String type) {
						if(opcode == Opcodes.CHECKCAST) {
							this.visitVarInsn(Opcodes.ILOAD, 1);
							this.visitVarInsn(Opcodes.ALOAD, 0);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "checkGetTileEntity", "(Lnet/minecraft/tileentity/TileEntity;BLcofh/thermaldynamics/block/TileMultiBlock;)Lnet/minecraft/tileentity/TileEntity;", false);
						}
						super.visitTypeInsn(opcode, type);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		
		ClassWriter writer = new ClassWriter(0/*ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES*/);
		node.accept(writer);
		return writer.toByteArray();
	}
	
}