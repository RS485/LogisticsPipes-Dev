package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.Collections;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.SecurityAddCCIdPacket;
import logisticspipes.network.packets.block.SecurityRemoveCCIdPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiEditCCAccessTable extends SubGuiScreen {

	private static final String PREFIX = "gui.securitystation.popup.ccAccess.";

	private final LogisticsSecurityTileEntity _tile;

	private String searchinput1 = "0";
	private String searchinput2 = "";
	private boolean editsearch = false;
	private boolean editsearchb = false;
	private boolean displaycursor = true;
	private long oldSystemTime = 0;
	private static int searchWidth = 55;
	private int lastClickedx = 0;
	private int lastClickedy = 0;
	private int lastClickedk = 0;
	private boolean clickWasButton = false;
	private int page = 0;

	public GuiEditCCAccessTable(LogisticsSecurityTileEntity tile) {
		super(150, 150, 0, 0);
		_tile = tile;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiLeft + 10, guiTop + 119, 30, 20, "-"));
		buttonList.add(new GuiButton(1, guiLeft + 110, guiTop + 119, 30, 20, "+"));
		buttonList.add(new SmallGuiButton(2, guiLeft + 30, guiTop + 107, 40, 10, TextUtil.translate(GuiEditCCAccessTable.PREFIX + "Remove")));
		buttonList.add(new SmallGuiButton(3, guiLeft + 80, guiTop + 107, 40, 10, TextUtil.translate(GuiEditCCAccessTable.PREFIX + "Add")));
		buttonList.add(new SmallGuiButton(4, guiLeft + 87, guiTop + 4, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(5, guiLeft + 130, guiTop + 4, 10, 10, ">"));
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		mc.fontRenderer.drawString("(" + (page + 1) + "/" + ((int) ((_tile.excludedCC.size() / 9D) + 1 - (_tile.excludedCC.size() % 9 == 0 && _tile.excludedCC.size() != 0 ? 1 : 0))) + ")", guiLeft + 100, guiTop + 5, 0x4F4F4F);

		boolean dark = true;
		for (int i = 0; i < 9; i++) {
			drawRect(guiLeft + 10, guiTop + 15 + (i * 10), right - 10, guiTop + 25 + (i * 10), dark ? Color.DARKER_GREY : Color.LIGHTER_GREY);
			dark = !dark;
		}
		dark = true;
		for (int i = 0; i < 9 && i + (page * 9) < _tile.excludedCC.size(); i++) {
			Integer id = _tile.excludedCC.get(i + (page * 9));
			mc.fontRenderer.drawString(Integer.toString(id), guiLeft + 75 - (mc.fontRenderer.getStringWidth(Integer.toString(id)) / 2), guiTop + 16 + (i * 10), dark ? 0xFFFFFF : 0x000000);
			dark = !dark;
			if (lastClickedx >= guiLeft + 10 && lastClickedx < right - 10 && lastClickedy >= guiTop + 15 + (i * 10) && lastClickedy < guiTop + 25 + (i * 10)) {
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				searchinput1 = Integer.toString(id);
				searchinput2 = "";
			}
		}

		//SearchInput
		if (editsearch) {
			drawRect(guiLeft + 40, bottom - 30, right - 40, bottom - 13, Color.BLACK);
			drawRect(guiLeft + 41, bottom - 29, right - 41, bottom - 14, Color.WHITE);
		} else {
			drawRect(guiLeft + 41, bottom - 29, right - 41, bottom - 14, Color.BLACK);
		}
		drawRect(guiLeft + 42, bottom - 28, right - 42, bottom - 15, Color.DARKER_GREY);

		mc.fontRenderer.drawString(searchinput1 + searchinput2, guiLeft + 75 - (mc.fontRenderer.getStringWidth(searchinput1 + searchinput2) / 2), bottom - 25, 0xFFFFFF);
		if (editsearch) {
			int linex = guiLeft + 75 + mc.fontRenderer.getStringWidth(searchinput1) - (mc.fontRenderer.getStringWidth(searchinput1 + searchinput2) / 2);
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				drawRect(linex, bottom - 27, linex + 1, bottom - 16, Color.WHITE);
			}
		}

		//Click into search
		if (lastClickedx != -10000000 && lastClickedy != -10000000) {
			if (lastClickedx >= guiLeft + 42 && lastClickedx < right - 42 && lastClickedy >= bottom - 30 && lastClickedy < bottom - 13) {
				editsearch = true;
				if (searchinput1.equals("0") && searchinput2.length() == 0) {
					searchinput1 = "";
				}
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				if (lastClickedk == 1) {
					searchinput1 = "0";
					searchinput2 = "";
				}
			} else {
				editsearch = false;
				if (searchinput1.length() == 0 && searchinput2.length() == 0) {
					searchinput1 = "0";
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		clickWasButton = false;
		editsearchb = true;
		super.mouseClicked(i, j, k);
		if ((!clickWasButton && i >= guiLeft + 10 && i < right - 10 && j >= guiTop + 18 && j < bottom - 10) || editsearch) {
			if (!editsearchb) {
				editsearch = false;
			}
			lastClickedx = i;
			lastClickedy = j;
			lastClickedk = k;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (editsearch) {
			editsearchb = false;
		}
		clickWasButton = true;
		switch (guibutton.id) {
			case 0:
				if ((searchinput1 + searchinput2).equals("")) {
					searchinput1 = "0";
					break;
				}
				try {
					int number = Integer.valueOf(searchinput1 + searchinput2);
					number--;
					if (number < 0) {
						number = 0;
					}
					searchinput1 = Integer.toString(number);
					searchinput2 = "";
				} catch (Exception e) {
					e.printStackTrace();
					searchinput1 = "0";
					searchinput2 = "";
				}
				break;
			case 1:
				if ((searchinput1 + searchinput2).equals("")) {
					searchinput1 = "1";
					break;
				}
				try {
					int number = Integer.valueOf(searchinput1 + searchinput2);
					number++;
					if (mc.fontRenderer.getStringWidth(Integer.toString(number)) <= GuiEditCCAccessTable.searchWidth) {
						searchinput1 = Integer.toString(number);
						searchinput2 = "";
					}
				} catch (Exception e) {
					e.printStackTrace();
					searchinput1 = "0";
					searchinput2 = "";
				}
				break;
			case 2: {
				Integer id = Integer.valueOf(searchinput1 + searchinput2);
				_tile.excludedCC.remove(id);
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityRemoveCCIdPacket.class).setInteger(id).setBlockPos(_tile.getPos()));
			}
			break;
			case 3: {
				Integer id = Integer.valueOf(searchinput1 + searchinput2);
				if (!_tile.excludedCC.contains(id)) {
					_tile.excludedCC.add(id);
					Collections.sort(_tile.excludedCC);
				}
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityAddCCIdPacket.class).setInteger(id).setBlockPos(_tile.getPos()));
			}
			break;
			case 4:
				page--;
				if (page < 0) {
					page = 0;
				}
				break;
			case 5:
				page++;
				if (page > (_tile.excludedCC.size() / 9) - (_tile.excludedCC.size() % 9 == 0 && _tile.excludedCC.size() != 0 ? 1 : 0)) {
					page = (_tile.excludedCC.size() / 9) - (_tile.excludedCC.size() % 9 == 0 && _tile.excludedCC.size() != 0 ? 1 : 0);
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (editsearch) {
			if (c == 13) {
				editsearch = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				try {
					Integer.valueOf(GuiScreen.getClipboardString());
					searchinput1 = searchinput1 + GuiScreen.getClipboardString();
				} catch (Exception e) {
					setSubGui(new GuiMessagePopup("Clipboard doesn't", "contain a number."));
				}
			} else if (c == 8) {
				if (searchinput1.length() > 0) {
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				}
				return;
			} else if (Character.isDigit(c)) {
				if (mc.fontRenderer.getStringWidth(searchinput1 + c + searchinput2) <= GuiEditCCAccessTable.searchWidth) {
					searchinput1 += c;
				}
				return;
			} else if (i == 203) { //Left
				if (searchinput1.length() > 0) {
					searchinput2 = searchinput1.substring(searchinput1.length() - 1) + searchinput2;
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				}
			} else if (i == 205) { //Right
				if (searchinput2.length() > 0) {
					searchinput1 += searchinput2.substring(0, 1);
					searchinput2 = searchinput2.substring(1);
				}
			} else if (i == 1) { //ESC
				editsearch = false;
			} else if (i == 28) { //Enter
				editsearch = false;
			} else if (i == 199) { //Pos
				searchinput2 = searchinput1 + searchinput2;
				searchinput1 = "";
			} else if (i == 207) { //Ende
				searchinput1 = searchinput1 + searchinput2;
				searchinput2 = "";
			} else if (i == 211) { //Entf
				if (searchinput2.length() > 0) {
					searchinput2 = searchinput2.substring(1);
				}
			}
		} else {
			super.keyTyped(c, i);
		}
	}

	public void drawRect(int x1, int y1, int x2, int y2, Color color) {
		Gui.drawRect(x1, y1, x2, y2, Color.getValue(color));
	}
}
