package client.swing.components.actionBtn;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ButtonsPanel extends JPanel {
    public final JButton editBtn = new JButton();
    public final JButton delBtn = new JButton();

    public ButtonsPanel(){
        setLayout(new GridLayout(1, 2, 0, 0));
        
        // Убираем внутренние поля кнопок, чтобы они были "впритык"
        editBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        delBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        // Чтобы кнопки не слипались совсем, можно добавить небольшой Padding САМОЙ панели
        setBorder(new EmptyBorder(2, 2, 2, 2));

        add(editBtn);
        add(delBtn);
        
        setOpaque(true);
    }
    
}
