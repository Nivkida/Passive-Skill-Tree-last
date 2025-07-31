package daripher.skilltree.client.widget.editor.menu.selection;

import daripher.skilltree.client.widget.Button;
import daripher.skilltree.client.widget.SelectionList;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class SelectionMenuButton<T> extends Button {
  private final SelectionList<T> selectionList;
  private Runnable onMenuInit = () -> {};
  private Consumer<T> responder = t -> {};

  public SelectionMenuButton(
      SkillTreeEditor editor, int x, int y, int width, String message, Collection<T> values) {
    super(x, y, width, 14, Component.literal(message));
    this.selectionList = new SelectionList<>(0, 0, 200, values).setMaxDisplayed(8);
    setPressFunc(b -> selectMenu(editor));
  }

  public SelectionMenuButton(
      SkillTreeEditor editor, int x, int y, int width, Collection<T> values) {
    this(editor, x, y, width, "", values);
  }

  public SelectionMenuButton<T> setResponder(Consumer<T> responder) {
    this.responder = responder;
    return this;
  }

  public SelectionMenuButton<T> setValue(T value) {
    selectionList.setValue(value);
    return this;
  }

  public SelectionMenuButton<T> setElementNameGetter(Function<T, Component> nameGetter) {
    selectionList.setNameGetter(nameGetter);
    T value = selectionList.getValue();
    if (getMessage().getString().isEmpty() && value != null) {
      setMessage(selectionList.getNameGetter().apply(value));
    }
    return this;
  }

  public void setMenuInitFunc(Runnable onMenuInit) {
    this.onMenuInit = onMenuInit;
  }

  private void selectMenu(SkillTreeEditor editor) {
    SelectionMenu<T> menu =
        new SelectionMenu<>(editor, editor.getSelectedMenu(), selectionList, onMenuInit)
            .setResponder(responder);
    editor.selectMenu(menu);
  }
}
