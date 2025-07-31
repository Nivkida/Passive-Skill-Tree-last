package daripher.skilltree.client.widget.editor.menu.selection;

import daripher.skilltree.client.widget.SelectionList;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectionMenu<T> extends EditorMenu {
  private @NotNull Consumer<T> responder = v -> {};
  private final SelectionList<T> selectionList;
  private final Runnable onInit;

  public SelectionMenu(
      SkillTreeEditor editor,
      @Nullable EditorMenu previousMenu,
      SelectionList<T> selectionList,
      Runnable onInit) {
    super(editor, previousMenu);
    this.selectionList = selectionList;
    this.onInit = onInit;
  }

  @Override
  public void init() {
    clearWidgets();
    editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> editor.selectMenu(previousMenu));
    editor.increaseHeight(29);
    selectionList.setX(editor.getWidgetsX(0));
    selectionList.setY(editor.getWidgetsY(0));
    editor.increaseHeight(selectionList.getMaxDisplayed() * 14 + 10);
    selectionList.setResponder(responder);
    addWidget(selectionList);
    onInit.run();
  }

  public SelectionMenu<T> setResponder(@NotNull Consumer<T> responder) {
    this.responder = responder;
    return this;
  }
}
