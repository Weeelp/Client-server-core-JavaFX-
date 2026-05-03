package client.swing.components.actionBtn;

import java.util.LinkedList;
import java.util.ResourceBundle;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import client.guiApp.impl.GuiAppSwing;
import client.manager.AuthManager;
import common.Request;
import common.model.movie.Movie;

public class ActionCell extends AbstractCellEditor implements TableCellRenderer,TableCellEditor {
    private final ButtonsPanel panel = new ButtonsPanel();
    private final AuthManager am;
    private final ResourceBundle bundle;
    private final GuiAppSwing gui;
    private int currentRow;

    public ActionCell(GuiAppSwing gui, AuthManager am, ResourceBundle bundle, ObjectMapper mapper) {
        this.am = am;
        this.bundle = bundle;
        this.gui = gui;

        panel.setFocusable(false);
        panel.editBtn.setFocusable(false);
        panel.delBtn.setFocusable(false);
        panel.editBtn.setRequestFocusEnabled(false);
        panel.delBtn.setRequestFocusEnabled(false);

        panel.editBtn.addActionListener(e -> {
            if (currentRow >= 0 && currentRow < gui.getSafeList().size()) {
                Movie m = gui.getSafeList().get(currentRow);
                gui.showMovieForm(m, 0); 
            }
            fireEditingStopped();
        });

        panel.delBtn.addActionListener(e -> {
            if (currentRow >= 0 && currentRow < gui.getSafeList().size()) {
                Movie m = gui.getSafeList().get(currentRow);
                Request req = new Request("remove_by_id", new String[]{String.valueOf(m.getId())}, null, new String[]{am.getLogin(), am.getPassword()});
                gui.getWorker().addTask(req, resp -> {if (resp.getStatus().equals("200")) {
                    Request showReq = new Request("show", null, null, new String[]{am.getLogin(), am.getPassword()});
                    
                    gui.getWorker().addTask(showReq, showResp -> {
                        LinkedList<Movie> newList = mapper.convertValue(showResp.getData(), 
                            new TypeReference<LinkedList<Movie>>() {});
                        gui.updateTableData(newList); 
                    });
                } else {
                    gui.showError(resp.getMessage());
                }});
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.currentRow = table.convertRowIndexToModel(row);
        if (this.currentRow < 0 || this.currentRow >= gui.getSafeList().size()) {
            panel.editBtn.setVisible(false);
            panel.delBtn.setVisible(false);
            return panel; 
        }

        panel.editBtn.setVisible(true);
        panel.delBtn.setVisible(true);
        
        Movie m = gui.getSafeList().get(this.currentRow);
        boolean isOwner = m.getOwner_login().equals(am.getLogin());

        panel.editBtn.setEnabled(isOwner);
        panel.delBtn.setEnabled(isOwner);
        panel.editBtn.setText(bundle.getString("btn_edit"));
        panel.delBtn.setText(bundle.getString("btn_del"));
        
        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return panel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentRow = table.convertRowIndexToModel(row);
        return this.panel;
    }

    @Override
    public Object getCellEditorValue() { return null; }
}
    
