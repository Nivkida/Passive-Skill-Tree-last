package daripher.skilltree.client.widget;

import daripher.skilltree.client.tooltip.TooltipHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class SelectionList<T> extends AbstractButton {
  public static final ResourceLocation WIDGETS_TEXTURE =
      new ResourceLocation("skilltree:textures/screen/widgets.png");
  private static final int LINE_HEIGHT = 14;
  private Function<T, Component> nameGetter = t -> Component.literal(t.toString());
  private Consumer<T> responder = t -> {};
  private final List<T> valuesList;
  private String search = "";
  private T value;
  private int maxDisplayed;
  private int maxScroll;
  private int scroll;

  public SelectionList(int x, int y, int width, Collection<T> possibleValues) {
    super(x, y, width, LINE_HEIGHT, Component.empty());
    this.valuesList = new ArrayList<>(possibleValues);
    setMaxDisplayed(10);
  }

  @Override
  public void onPress() {
    responder.accept(value);
  }

  @Override
  public void renderWidget(
      @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) return;
    int y = this.getY();
    renderLineBackground(graphics, y, 42, LINE_HEIGHT / 2);
    for (int i = 0; i < maxDisplayed - 1; i++) {
      int rowY = y + LINE_HEIGHT / 2 + i * LINE_HEIGHT;
      renderLineBackground(graphics, rowY, 70, LINE_HEIGHT);
    }
    y += (maxDisplayed - 1) * LINE_HEIGHT + LINE_HEIGHT / 2;
    renderLineBackground(graphics, y, 42 + LINE_HEIGHT / 2, LINE_HEIGHT / 2);
    y -= (maxDisplayed - 1) * LINE_HEIGHT + LINE_HEIGHT / 2;
    Minecraft minecraft = Minecraft.getInstance();
    Font font = minecraft.font;
    for (int i = 0; i < maxDisplayed; i++) {
      renderLine(graphics, i, y, font);
    }
    renderScroll(graphics);
  }

  private void renderLine(@NotNull GuiGraphics graphics, int line, int y, Font font) {
    List<T> values = getValues();
    if (line + scroll >= values.size()) return;
    String text = nameGetter.apply(values.get(line + scroll)).getString();
    String selectedText = nameGetter.apply(value).getString();
    int textColor = text.equals(selectedText) ? 0x55ff55 : 0xe0e0e0;
    text = TooltipHelper.getTrimmedString(text, width - 10);
    int textX = getX() + 5;
    int textY = y + 3 + line * LINE_HEIGHT;
    renderLine(graphics, font, text, textX, textY, textColor);
  }

  private void renderLine(
      @NotNull GuiGraphics graphics, Font font, String line, int textX, int textY, int textColor) {
    String lowerCase = line.toLowerCase();
    if (!search.isEmpty() && lowerCase.contains(search)) {
      String split1 = line.substring(0, lowerCase.indexOf(search));
      graphics.drawString(font, split1, textX, textY, textColor);
      textX += font.width(split1);
      String split2 =
          line.substring(lowerCase.indexOf(search), lowerCase.indexOf(search) + search.length());
      graphics.drawString(font, split2, textX, textY, 0xFFD642);
      textX += font.width(split2);
      String split3 = line.substring(lowerCase.indexOf(search) + search.length());
      graphics.drawString(font, split3, textX, textY, textColor);
    } else {
      graphics.drawString(font, line, textX, textY, textColor);
    }
  }

  private List<T> getValues() {
    if (!search.isEmpty()) {
      return valuesList.stream().filter(this::isSearched).toList();
    }
    return valuesList;
  }

  private boolean isSearched(T value) {
    return nameGetter.apply(value).getString().toLowerCase().contains(search);
  }

  private void renderLineBackground(
      @NotNull GuiGraphics graphics, int rowY, int vOffset, int height) {
    graphics.blit(WIDGETS_TEXTURE, getX(), rowY, 0, vOffset, width / 2, height);
    graphics.blit(
        WIDGETS_TEXTURE, getX() + width / 2, rowY, -width / 2, vOffset, width / 2, height);
  }

  private void renderScroll(GuiGraphics graphics) {
    if (getValues().size() <= maxDisplayed) return;
    int maxScrollSize = height - 8;
    int scrollSize = maxScrollSize * maxDisplayed / getValues().size();
    int x = getX() + width - 4;
    int y = getY() + 3 + (maxScrollSize - scrollSize) * scroll / maxScroll;
    graphics.fill(x, y, x + 1, y + scrollSize + 1, 0xffaaaaaa);
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    if (!clicked(mouseX, mouseY)) return;
    int clickedLine = ((int) mouseY - getY()) / LINE_HEIGHT + scroll;
    List<T> values = getValues();
    if (clickedLine >= values.size()) return;
    value = values.get(clickedLine);
    onPress();
  }

  @Override
  public void mouseMoved(double mouseX, double mouseY) {
    super.mouseMoved(mouseX, mouseY);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (isMouseOver(mouseX, mouseY)) {
      setScroll(scroll - Mth.sign(delta));
      return true;
    }
    return false;
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (!isHoveredOrFocused()) return false;
    if (!SharedConstants.isAllowedChatCharacter(codePoint)) return false;
    search += Character.toLowerCase(codePoint);
    setScrollToSelection();
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (search.isEmpty()) return false;
    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
      search = search.substring(0, search.length() - 1);
      setScrollToSelection();
      return true;
    }
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      search = "";
      setScrollToSelection();
      return true;
    }
    return false;
  }

  private void setScroll(int scroll) {
    this.scroll = Math.min(maxScroll, Math.max(0, scroll));
  }

  public SelectionList<T> setNameGetter(Function<T, Component> nameGetter) {
    this.nameGetter = nameGetter;
    getValues()
        .sort(
            (v1, v2) -> {
              String name1 = nameGetter.apply(v1).getString();
              String name2 = nameGetter.apply(v2).getString();
              return name1.compareTo(name2);
            });
    setScrollToSelection();
    return this;
  }

  public Function<T, Component> getNameGetter() {
    return nameGetter;
  }

  public SelectionList<T> setResponder(Consumer<T> responder) {
    this.responder = responder;
    return this;
  }

  public T getValue() {
    return value;
  }

  public SelectionList<T> setValue(T value) {
    this.value = value;
    setScrollToSelection();
    return this;
  }

  public int getMaxDisplayed() {
    return maxDisplayed;
  }

  public SelectionList<T> setMaxDisplayed(int maxDisplayed) {
    maxDisplayed = Math.min(maxDisplayed, getValues().size());
    this.maxDisplayed = maxDisplayed;
    this.maxScroll = getValues().size() - maxDisplayed;
    setHeight(LINE_HEIGHT * maxDisplayed);
    return this;
  }

  public void setScrollToSelection() {
    setScroll(getValues().indexOf(value));
  }

  @Override
  protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
}
