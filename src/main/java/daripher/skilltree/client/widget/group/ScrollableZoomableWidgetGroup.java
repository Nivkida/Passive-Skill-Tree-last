package daripher.skilltree.client.widget.group;

import java.awt.geom.Rectangle2D;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ScrollableZoomableWidgetGroup<T extends AbstractWidget> extends WidgetGroup<T> {
  protected float scrollSpeedX;
  protected float scrollSpeedY;
  protected float scrollX;
  protected float scrollY;
  protected int maxScrollX;
  protected int maxScrollY;
  private float zoom = 1F;

  public ScrollableZoomableWidgetGroup(int pX, int pY, int pWidth, int pHeight) {
    super(pX, pY, pWidth, pHeight);
  }

  @Override
  protected void renderWidget(
      @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    updateScroll(partialTick);
    graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
    graphics.pose().pushPose();
    graphics.pose().translate(scrollX, scrollY, 0);
    renderBackground(graphics, mouseX, mouseY, partialTick);
    for (T widget : widgets) {
      graphics.pose().pushPose();
      double widgetCenterX = widget.getX() + widget.getWidth() / 2f;
      double widgetCenterY = widget.getY() + widget.getHeight() / 2f;
      graphics.pose().translate(widgetCenterX, widgetCenterY, 0F);
      graphics.pose().scale(zoom, zoom, 1F);
      graphics.pose().translate(-widgetCenterX, -widgetCenterY, 0F);
      widget.render(graphics, mouseX, mouseY, partialTick);
      graphics.pose().popPose();
    }
    graphics.pose().popPose();
    graphics.disableScissor();
  }

  protected void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;
    if (maxScrollX > 0) scrollSpeedX += (float) (dragX * 0.25f);
    if (maxScrollY > 0) scrollSpeedY += (float) (dragY * 0.25f);
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (delta > 0 && zoom < 2F) zoom += 0.05f;
    if (delta < 0 && zoom > 0.25F) zoom -= 0.05f;
    rebuildFunc.run();
    return true;
  }

  public @Nullable T getWidgetAt(double mouseX, double mouseY) {
    mouseX -= scrollX;
    mouseY -= scrollY;
    for (T widget : widgets) {
      Rectangle2D.Double widgetArea = getWidgetArea(widget);
      if (widgetArea.contains(mouseX, mouseY)) return widget;
    }
    return null;
  }

  @NotNull
  protected Rectangle2D.Double getWidgetArea(T widget) {
    double width = widget.getWidth() * zoom;
    double height = widget.getHeight() * zoom;
    double x = widget.getX() + widget.getWidth() / 2d - width / 2;
    double y = widget.getY() + widget.getHeight() / 2d - height / 2;
    return new Rectangle2D.Double(x, y, width, height);
  }

  private void updateScroll(float partialTick) {
    scrollX += scrollSpeedX * partialTick;
    scrollX = Math.max(-maxScrollX * zoom, Math.min(maxScrollX * zoom, scrollX));
    scrollSpeedX *= 0.8f;
    scrollY += scrollSpeedY * partialTick;
    scrollY = Math.max(-maxScrollY * zoom, Math.min(maxScrollY * zoom, scrollY));
    scrollSpeedY *= 0.8f;
  }

  public void setMaxScrollX(int maxScrollX) {
    this.maxScrollX = maxScrollX;
  }

  public void setMaxScrollY(int maxScrollY) {
    this.maxScrollY = maxScrollY;
  }

  public int getMaxScrollX() {
    return maxScrollX;
  }

  public int getMaxScrollY() {
    return maxScrollY;
  }

  public float getScrollX() {
    return scrollX;
  }

  public float getScrollY() {
    return scrollY;
  }

  public float getZoom() {
    return zoom;
  }
}
