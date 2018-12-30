package ua.itea.filenamager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.sql.Date;
import java.util.*;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileManager extends JFrame {

    private JList<String> list = new JList<>();
    private String path = System.getProperty("user.dir").replace('\\', '/');
    private JTextField tf = new JTextField();
    private JTextPane tp = new JTextPane();
    private JFileChooser chooser = new JFileChooser();
    private List<String> txtList = Arrays.asList("txt", "log", "java", "xml", "properties", "gitignore", "sh", "ini");
    private List<String> imgList = Arrays.asList("jpg", "jpeg", "png", "ico", "bmp");
    private String aboutText = "Fileger © Tymoshok Dmytro \n" +
            "Main functional : Done \n" +
            "DIR marker aside of folders in list : Done \n" +
            "Arrows navigation + Enter key : Done \n" +
            "Mouse actions like one or double click : Done \n" +
            "One click shows the info about file or dir : Done \n" +
            "Double click or Enter on text or image file \nshows the content : Done \n" +
            "Action buttons Copy, Move, New Folder + key strokes : Done \n" +
            "Action button Delete : Done \n" +
            "Return to previous folder with Backspace : Done \n";

    private void alertError(Exception e) {
        e.printStackTrace();
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        JOptionPane.showMessageDialog(null,
                writer.toString(),
                "Ooops! Error occured.",
                JOptionPane.ERROR_MESSAGE);
    }

    private void folderAction() {
        String sel = list.getSelectedValue();
        if (sel != null) {
            if (sel.equals("src/main")) {
                String dir = new File(path).getParent();
                if (dir != null) {
                    path = dir;
                    list.setListData(getDirList(path));
                    list.setSelectedIndex(0);
                }
            }
            if (sel.contains("DIR")) {
                String dir = sel.replace("DIR", "").trim();
                if (new File(path + "/" + dir).exists()) {
                    path = path.equals("/") ? "/" + dir : path + "/" + dir;
                    list.setListData(getDirList(path));
                    list.setSelectedIndex(0);
                }
            } else {
                sel = sel.replace("DIR", "").trim();
                File f = new File(path + "/" + sel);
                if (f.isFile()) {
                    String ext = (f.getName().substring(f.getName().lastIndexOf("") + 1));
                    if (txtList.contains(ext)) {
                        try {
                            Scanner in = new Scanner(f);
                            in.useDelimiter("\n");
                            StringBuilder sb = new StringBuilder();
                            while (in.hasNext()) {
                                sb.append(in.next()).append("\n");
                            }
                            tp.setText(sb.toString());
                        } catch (FileNotFoundException e) {
                            JOptionPane.showMessageDialog(this, path + "/" + sel + " not found", "", JOptionPane.WARNING_MESSAGE);
                            e.printStackTrace();
                        }
                    }
                    if (imgList.contains(ext)) {
                        tp.setText("");
                        tp.insertIcon(new ImageIcon(path + "/" + sel));
                    }
                }
            }
            tf.setText(path);
        }
    }

    private String getFileInfoString(File file) {
        return "Name: " + file.getName() + "\n" +
                "Absolute path: " + file.getAbsolutePath() + "\n" +
                "Size: " + file.length() + " bytes" + "\n" +
                (file.isDirectory() ? "Type: DIR" : "Type: FILE") + "\n" +
                "Hidden?: " + file.isHidden() + "\n" +
                "LastModified: " + new Date(file.lastModified()) + "\n";
    }

    private void getInfoAction() {
        String sel = list.getSelectedValue().replace("DIR", "").trim();
        if (sel != null) {
            if (!sel.equals("src/main")) {
                tp.setText(getFileInfoString(new File(path + "/" + sel)));
            }
        }
    }

    private String[] getDirList(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                List<String> res = new ArrayList<>();
                List<String> fres = new ArrayList<>();

                res.add("src/main");
                String fname;
                int fnameMaxLength = 45;
                for (File f : Objects.requireNonNull(files)) {
                    fname = f.getName();
                    if (f.isDirectory()) {
                        res.add(String.format("%-" + fnameMaxLength + "s%-10s", fname, "DIR"));

                    }
                }
                res.sort(Comparator.naturalOrder());
                for (File f : files) {
                    if (f.isFile()) {
                        fres.add(f.getName());
                    }
                }
                fres.sort(Comparator.naturalOrder());
                res.addAll(fres);
                return res.toArray(new String[0]);
            } else {
                JOptionPane.showMessageDialog(null, path + " is not a directory");
                System.exit(0);
            }
        } else {
            JOptionPane.showMessageDialog(null, path + " is not exists");
            System.exit(0);
        }
        return null;
    }

    private void deleteDir(Path dir) throws IOException {
        Files.walk(dir, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(System.out::println)
                .forEach(File::delete);
    }

    private void deleteAction() throws IOException {
        String value = list.getSelectedValue();
        if (value != null) {
            if (!value.equals("src/main")) {
                value = value.replace("DIR", "").trim();
                Path selFile = Paths.get(path + "/" + value);
                if (Files.exists(selFile)) {
                    int ok = JOptionPane.showConfirmDialog(null,
                            "Do you really to delete the \"" + value + "\" ?",
                            "Delete confirmation", JOptionPane.OK_CANCEL_OPTION);
                    if (ok == 0) {
                        if (Files.isDirectory(selFile)) {
                            deleteDir(selFile);
                        } else {
                            Files.deleteIfExists(selFile);
                        }
                        list.setListData(getDirList(path));
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "The selected folder path \"" + selFile.toString() + "\" does not exists");
                }

            } else {
                JOptionPane.showMessageDialog(null, "Cannot delete the \"..\" (It's kind of dumb operation, dont you think?)");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nothing is selected");
        }
        list.requestFocus();
        list.setSelectedIndex(0);
    }

    private void copyMove(ActionEvent e) {
        String value = list.getSelectedValue();
        if (value != null) {
            if (!value.equals("src/main")) {
                chooser.setCurrentDirectory(new File(path));
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selFile = chooser.getSelectedFile();
                    if (selFile.exists()) {
                        if (selFile.isDirectory()) {
                            if (value.contains("DIR")) {
                                value = value.replace("DIR", "").trim();
                                try {
                                    Path srcDir = Paths.get(path + "/" + value);
                                    Path destDir = Paths.get(selFile.getAbsolutePath() + "/" + value);
                                    Files.walkFileTree(srcDir, new CopyDir(srcDir, destDir));
                                    if (e.getActionCommand().equals("move")) {
                                        deleteDir(srcDir);
                                        list.setListData(getDirList(path));
                                        JOptionPane.showMessageDialog(null, "The selected folder \"" + value + "\" successfully moved");
                                    } else {
                                        JOptionPane.showMessageDialog(null, "The selected folder \"" + value + "\" successfully copied");
                                    }

                                } catch (FileAlreadyExistsException ignored) {

                                } catch (IOException e1) {
                                    alertError(e1);
                                }
                            } else {
                                if (new File(selFile + "/" + value).exists()) {
                                    int ok = JOptionPane.showConfirmDialog(null,
                                            "The file \"" + value + "\" already exists in the destination folder. \nDo you want to overwrite it ?",
                                            "File exists alert", JOptionPane.OK_CANCEL_OPTION);
                                    try {
                                        if (ok == 0) {

                                            if (e.getActionCommand().equals("copy")) {
                                                Files.copy(Paths.get(path + "/" + value), Paths.get(selFile + "/" + value), REPLACE_EXISTING);
                                                JOptionPane.showMessageDialog(null, "The selected file \"" + value + "\" successfully copied");
                                            }
                                            if (e.getActionCommand().equals("move")) {
                                                Files.move(Paths.get(path + "/" + value), Paths.get(selFile + "/" + value), REPLACE_EXISTING);
                                                list.setListData(getDirList(path));
                                                JOptionPane.showMessageDialog(null, "The selected file \"" + value + "\" successfully moved");
                                            }

                                        }
                                        if (ok == 2) {
                                            if (e.getActionCommand().equals("copy")) {
                                                Files.copy(Paths.get(path + "/" + value), Paths.get(selFile + "/" + value.substring(0, value.lastIndexOf("")) + "(1)" + value.substring(value.lastIndexOf(""))), REPLACE_EXISTING);
                                                JOptionPane.showMessageDialog(null, "The selected file \"" + value + "\"(1) successfully copied");
                                                list.setListData(getDirList(path));
                                            }
                                        }
                                    } catch (IOException e1) {
                                        alertError(e1);
                                    }
                                } else {
                                    try {
                                        if (e.getActionCommand().equals("copy")) {
                                            Files.copy(Paths.get(path + "/" + value), Paths.get(selFile + "/" + value), REPLACE_EXISTING);
                                            JOptionPane.showMessageDialog(null, "The selected file \"" + selFile.toString() + "\" successfully copied");
                                        }
                                        if (e.getActionCommand().equals("move")) {
                                            Files.move(Paths.get(path + "/" + value), Paths.get(selFile + "/" + value), REPLACE_EXISTING);
                                            list.setListData(getDirList(path));
                                            JOptionPane.showMessageDialog(null, "The selected file \"" + selFile.toString() + "\" successfully moved");
                                        }
                                    } catch (IOException e1) {
                                        alertError(e1);
                                    }
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "The selected folder path \"" + selFile + "\" does not exists");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "The selected folder path \"" + selFile + "\" is not a directory");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Cannot copy the \"..\" (It's kind of dumb operation, dont you think?)");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nothing is selected");
        }
        list.requestFocus();
    }

    public FileManager() {
        setTitle("Fileger © Tymoshok Dmytro");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int width = 800;
        int height = 600;
        setSize(width, height);
        setLocation((int) (screenSize.getWidth() / 2) - (width / 2), (int) (screenSize.getHeight() / 2) - (height / 2));

        tp.setEditable(false);

        Dimension buttonDim = new Dimension(150, 25);
        JButton buttonCopy = new JButton("Copy F5");
        buttonCopy.setActionCommand("copy");
        buttonCopy.setPreferredSize(buttonDim);

        chooser.setDialogTitle("Select Folder to Copy");
        chooser.setSelectedFile(null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);


        ActionListener copyMoveActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyMove(e);
            }
        };

        buttonCopy.addActionListener(copyMoveActionListener);

        JButton buttonMove = new JButton("Move F6");
        buttonMove.setActionCommand("move");
        buttonMove.setPreferredSize(buttonDim);
        buttonMove.addActionListener(copyMoveActionListener);

        JButton buttonNewFolder = new JButton("New folder F7");
        buttonNewFolder.setPreferredSize(buttonDim);
        buttonNewFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newFolderName = JOptionPane.showInputDialog("Input the new folder's name:");
                try {
                    if (newFolderName.matches("[[\\w]~@#$%^\\-_(){}'`]+")) {
                        if (new File(path + "/" + newFolderName).mkdir()) {
                            list.setListData(getDirList(path));
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Name \"" + newFolderName + "\" not valid as a folder name");
                    }
                } catch (NullPointerException ignored) {

                }
                list.requestFocus();
            }
        });

        JButton buttonDelete = new JButton("Delete F8");
        buttonDelete.setPreferredSize(buttonDim);
        buttonDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    deleteAction();
                } catch (IOException e1) {
                    alertError(e1);
                }
            }
        });


        JButton buttonAbout = new JButton("About F1");
        buttonAbout.setPreferredSize(buttonDim);
        buttonAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, aboutText);
            }
        });

        String[] sl = getDirList(path);
        list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    getInfoAction();
                }
                if (evt.getClickCount() == 2) {
                    folderAction();
                }
            }
        });

        list.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    folderAction();
                } else if (code == KeyEvent.VK_BACK_SPACE) {
                    list.setSelectedValue("src/main", false);
                    folderAction();
                } else if (code == KeyEvent.VK_F1) {
                    buttonAbout.doClick();
                } else if (code == KeyEvent.VK_F5) {
                    buttonCopy.doClick();
                } else if (code == KeyEvent.VK_F6) {
                    buttonMove.doClick();
                } else if (code == KeyEvent.VK_F7) {
                    buttonNewFolder.doClick();
                } else if (code == KeyEvent.VK_F8) {
                    buttonDelete.doClick();
                } else if (code == KeyEvent.VK_UP && list.getSelectedValue() == null) {
                    list.setSelectedIndex(0);
                } else if (list.getSelectedValue() != null && (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN)) {
                    getInfoAction();
                }
                if (code == KeyEvent.VK_F5 ||
                        code == KeyEvent.VK_F6 ||
                        code == KeyEvent.VK_F7 ||
                        code == KeyEvent.VK_F8
                ) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });

        list.setListData(sl);

        JScrollPane scrollPane = new JScrollPane(tp);

        tf.setText(path);
        tf.setAction(
                new AbstractAction("Enter") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String tf_text = tf.getText();
                        if (new File(tf_text).exists()) {
                            if (new File(tf_text).isDirectory()) {
                                path = tf_text;
                                list.setListData(getDirList(path));
                            } else {
                                JOptionPane.showMessageDialog(null, tf.getText() + " is not a directory");
                            }

                        } else {
                            JOptionPane.showMessageDialog(null, tf.getText() + " doesn't exists");
                        }
                        tf.setText(path);
                    }
                }
        );

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), scrollPane);
        sp.setDividerSize(5);
        sp.setOneTouchExpandable(true);
        sp.setDividerLocation(width / 2);

        JSplitPane spv = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tf, sp) {
            private final int location = 25;

            {
                setDividerLocation(location);
                setDividerSize(5);
            }

            @Override
            public int getDividerLocation() {
                return location;
            }

            @Override
            public int getLastDividerLocation() {
                return location;
            }
        };

        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(buttonAbout);
        infoPanel.add(buttonCopy);
        infoPanel.add(buttonMove);
        infoPanel.add(buttonNewFolder);
        infoPanel.add(buttonDelete);

        int minusValue = 40;
        JSplitPane spDownBar = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spv, infoPanel) {
            {
                setDividerLocation(getHeight() - minusValue);
                setDividerSize(5);
            }

            @Override
            public int getDividerLocation() {
                return getHeight() - minusValue;
            }

            @Override
            public int getLastDividerLocation() {
                return getHeight() - minusValue;
            }

        };

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                spDownBar.setDividerLocation(getHeight() - minusValue);
                sp.setDividerLocation(getWidth() / 2);
            }
        });


        add(spDownBar);
        setVisible(true);

        list.requestFocus();
        list.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        new FileManager();
    }
}
