package iecompbot;

import iecompbot.objects.Kernel32;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.server.ServerInfo;
import iecompbot.springboot.data.DatabaseObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static iecompbot.Main.*;
import static iecompbot.interaction.Automation.isStartup;
import static my.utilities.util.Utilities.StopString;

public class Terminal extends JFrame {
    private final Image icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/logo.png"))).getImage();
    private final Image icononline = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/tray/logo16x16-online.png"))).getImage();
    private final Image iconoffline = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/tray/logo16x16-offline.png"))).getImage();
    private final Image iconwarning = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/tray/logo16x16-warning.png"))).getImage();
    private final Image iconattention = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/tray/logo16x16-attention.png"))).getImage();

    public JLabel mb = new JLabel("-", SwingConstants.CENTER);
    public int AllServers = 0;
    public JLabel AllServersLabel = new JLabel(String.valueOf(AllServers), SwingConstants.CENTER);
    public int ActiveServers = 0;
    public JLabel ActiveServersLabel = new JLabel(String.valueOf(ActiveServers), SwingConstants.CENTER);
    public int ActiveTournament = 0;
    public JLabel ActiveTournamentLabel = new JLabel(String.valueOf(ActiveTournament), SwingConstants.CENTER);
    public int ActiveUsers = 0;
    public JLabel ActiveUsersLabel = new JLabel(String.valueOf(ActiveUsers), SwingConstants.CENTER);
    public int ActiveTimeouts = 0;
    public JLabel ActiveTimeoutsLabel = new JLabel(String.valueOf(ActiveUsers), SwingConstants.CENTER);
    public int ActiveBans = 0;
    public JLabel ActiveBansLabel = new JLabel(String.valueOf(ActiveUsers), SwingConstants.CENTER);
    public String activitystring = "";
    public double ScreenMultiplier = 1.2;

    public JComboBox<String> StatusCB;
    public JComboBox<String> ActivityCB;
    public JTextField ActivityTF;


    public static JTextArea BotLog = new JTextArea();

    public PrintStream oldconsole = System.out;
    public static ByteArrayOutputStream Console = new ByteArrayOutputStream();


    public static int BatteryPercent = 100;


    public Terminal() {
        String[] statuses = {"OFFLINE", "ONLINE", "DO_NOT_DISTURB", "IDLE", "INVISIBLE"};
        String[] activity = {"Competing", "Playing", "Listening", "Watching"};
        StatusCB = new JComboBox<>(statuses);
        ActivityCB = new JComboBox<>(activity);
        ActivityTF = new JTextField("with <users> in <whitelist> !");

        BotLog.setFont(ConsoleFont);
        BotLog.setText("[BOT] Terminal launched");
        BotLog.setBorder(BorderFactory.createLineBorder(Color.black));
        BotLog.setSize(-1, -1);
        BotLog.setEditable(false);

        JPanel AboveAndBelow = new JPanel(); {
            JTabbedPane TwoSide = new JTabbedPane(); {
                JPanel P1 = new JPanel(); {
                    JPanel BotManager = new JPanel(); {
                        BotManager.setBounds(3, 3, (int) (240 * ScreenMultiplier), (int) (90 * ScreenMultiplier));
                        BotManager.setBorder(BorderFactory.createLineBorder(Color.gray));
                        BotManager.setLayout(new GridLayout(4, 2));
                        BotManager.add(new JLabel(" Bot Activator:"));
                        BotManager.add(getONOFFButton());
                        BotManager.add(new JLabel(" Bot Status:"));
                        BotManager.add(StatusCB);
                        BotManager.add(new JLabel(" Bot Activity Type:"));
                        BotManager.add(ActivityCB);
                        BotManager.add(new JLabel(" Bot Activity:"));
                        BotManager.add(ActivityTF);
                    }
                    JPanel StorageConsole = new JPanel(); {
                        StorageConsole.setBounds(BotManager.getWidth() + 5, 3, (int) (230 * ScreenMultiplier), (int) (90 * ScreenMultiplier));
                        StorageConsole.setLayout(new GridLayout(4, 2));
                        StorageConsole.setBorder(BorderFactory.createLineBorder(Color.gray));
                        StorageConsole.add(new JLabel(" Storage Data"));
                        StorageConsole.add(getBrowseButton());
                        StorageConsole.add(new JLabel(" Clear Console"));
                        StorageConsole.add(getClearConsoleButton());
                        StorageConsole.add(new JLabel(" Active Servers:"));
                        StorageConsole.add(ActiveServersLabel);
                        StorageConsole.add(new JLabel(" Memory Usage:"));
                        StorageConsole.add(mb);
                    }
                    P1.setLayout(null);
                    P1.setBounds(3, 3, (int) (225 * ScreenMultiplier), (int) (95 * ScreenMultiplier));
                    P1.add(BotManager);
                    P1.add(StorageConsole);
                }
                JPanel P2 = new JPanel(); {
                    JPanel moreOptions = new JPanel(); {
                        moreOptions.setBounds(3, 3, (int) (225 * ScreenMultiplier), (int) (90 * ScreenMultiplier));
                        moreOptions.setLayout(new GridLayout(4, 2));
                        moreOptions.setBorder(BorderFactory.createLineBorder(Color.gray));
                        moreOptions.add(new JLabel(" Auto-Launch:"));
                        moreOptions.add(getEnableAutoStart());
                        moreOptions.add(new JLabel(" Enable Console:"));
                        moreOptions.add(getEnableConsole());
                        moreOptions.add(new JLabel(" Search User"));
                        moreOptions.add(getSearchUserButton());
                        moreOptions.add(new JLabel(" View Rate Limits"));
                        moreOptions.add(getRateLimitButton());
                    }
                    P2.setLayout(null);
                    P2.setBounds(230, 0, (int) (225 * ScreenMultiplier), (int) (95 * ScreenMultiplier));
                    P2.add("More Options", moreOptions);
                }
                JPanel P3 = new JPanel(); {
                    P3.setLayout(new BoxLayout(P3, BoxLayout.Y_AXIS));
                    JPanel header = new JPanel();
                    header.setLayout(new GridLayout(1, 2));
                    header.setBackground(Color.gray);
                    header.setBorder(BorderFactory.createLineBorder(Color.darkGray));
                    header.add(new JLabel("Server Name"));
                    header.add(new JLabel("Owner"));
                    header.add(new JLabel("Total Tournaments"));
                    P3.add(header);
                    int count = 1;
                    for (ServerInfo serverInfo : ServerInfo.list()) {
                        if (serverInfo.MemberCount > 0) {
                            JPanel t = new JPanel();
                            t.setLayout(new GridLayout(1, 2));
                            JLabel l = FT(StopString(serverInfo.getName(), 24), 10);
                            t.add(l);
                            t.add(FT(StopString(serverInfo.getName(), 24), 10));
                            t.add(FT(String.valueOf(serverInfo.getID()), 10));
                            P3.add(t);
                            count = count + 1;
                            l.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent me) {
                                    try {
                                      //  Desktop.getDesktop().open(server);
                                    } catch (Exception e) {
                                        System.out.println("[Error] Directory no longer exists.");
                                    }
                                }
                            });
                        }
                    }
                }
                JPanel P4 = new JPanel(); {
                    P4.setLayout(new BoxLayout(P4, BoxLayout.Y_AXIS));
                    String[] column = {"Clan Name","Members","Nationality"};
                    String[][] data = new String[DatabaseObject.Count(Clan.class) + 1][3];
                    data[0][0] = "Clan Name";
                    data[0][1] = "Members";
                    data[0][2] = "Nationality";
                    int count = 1;
                    JTable Table = new JTable(data, column);
                    for (Clan clan : Clan.list()) {
                        data[count][0] = clan.getTag() + " | " + clan.getName();
                        data[count][1] = String.valueOf(clan.getMemberCount());
                        data[count][2] = String.valueOf(clan.getNationality());
                        int finalCount1 = count;
                        Table.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseReleased(java.awt.event.MouseEvent evt) {
                                int row = Table.rowAtPoint(evt.getPoint());
                                int col = Table.columnAtPoint(evt.getPoint());
                                if (row == finalCount1 && col == 0) {
                                    try {
                                        Desktop.getDesktop().open(new File(ClansDirectory.getPath() + "/" + clan.getName() + "/"));
                                        UpdateConsole();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
                        count = count + 1;
                    }
                    Table.setEnabled(false);
                    P4.add(Table);
                }

                TwoSide.addTab("Bot Manager", P1);
                TwoSide.addTab("Settings", P2);
                TwoSide.addTab("Servers", new JScrollPane(P3));
                TwoSide.addTab("Clans", new JScrollPane(P4));
            }
            AboveAndBelow.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(5, 3, 3, 3)));
            AboveAndBelow.setLayout(new GridLayout(2, 1));
            AboveAndBelow.add(TwoSide);
            AboveAndBelow.add(new JScrollPane(BotLog));
            BotLog.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    UpdateConsole();
                }
            });
        }



        mb.setText("<html><font color=green>0 MB</font></html>");
        mb.setFont(new Font("a", Font.BOLD, 10));

        setSize((int) (500 * ScreenMultiplier), (int) (270 * ScreenMultiplier));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setTitle("Bot Terminal");
        add(AboveAndBelow);
        setIconImage(icon);
        AddSystemTray();
        UpdateMemory();

        if (Prefs.isAppConsoleEnabled) {
            System.setOut(new PrintStream(Console));
            System.setErr(new PrintStream(Console));
        }
        if (Prefs.isAutoLaunchEnabled) {
            getONOFFButton().doClick();
        }
    }


    public JLabel FT(String s, int i) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("f", Font.BOLD, i));
        return l;
    }
    public static void UpdateConsole() {
        BotLog.setText(String.valueOf(Console));
    }
    
    public TrayIcon trayIcon;
    private void AddSystemTray() {
        try {
            if (SystemTray.isSupported()) {

                MenuItem ExitTrayButton = new MenuItem("Close Terminal");
                MenuItem SwitchTrayButton = new MenuItem("Turn ON Bot");
                MenuItem openfolder = new MenuItem("Open Bot Data Folder");

                ExitTrayButton.addActionListener(e -> System.exit(0));
                SwitchTrayButton.addActionListener(e -> getONOFFButton().doClick());
                openfolder.addActionListener(e -> getBrowseButton().doClick());

                PopupMenu popupMenu = new PopupMenu();
                popupMenu.add(SwitchTrayButton);
                popupMenu.add(openfolder);
                popupMenu.add(ExitTrayButton);

                trayIcon = new TrayIcon(iconoffline, "[Offline] Inazuma Competitive Terminal", popupMenu);
                trayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            setVisible(true);
                        }
                    }
                });
                SystemTray.getSystemTray().add(trayIcon);
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    public void UpdateMemory() {
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
        java.util.Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int BoardTimer = 3600 * 3;
            int RemindTimer = 3600 * 6;
            int delayChannel = 10;
            public void run(){
                BoardTimer = BoardTimer - 1;
                RemindTimer = RemindTimer - 1;
                delayChannel = delayChannel - 1;
                int mbb = 1024 * 1024;
                Runtime instance = Runtime.getRuntime();
                mb.setText((instance.totalMemory() - instance.freeMemory()) / mbb + " MB");
                if (((instance.totalMemory() - instance.freeMemory()) / mbb) > 80) {
                    mb.setText("<html><font color=red>" + mb.getText() + "</font></html>");
                    trayIcon.setImage(iconattention);
                    trayIcon.setToolTip("[Attention] Inazuma Competitive Terminal");
                } else if (((instance.totalMemory() - instance.freeMemory()) / mbb) > 60) {
                    mb.setText("<html><font color=orange>" + mb.getText() + "</font></html>");
                    trayIcon.setImage(iconwarning);
                    trayIcon.setToolTip("[Warning] Inazuma Competitive Terminal");
                } else if (((instance.totalMemory() - instance.freeMemory()) / mbb) > 40) {
                    mb.setText("<html><font color=yellow>" + mb.getText() + "</font></html>");
                    trayIcon.setImage(iconwarning);
                    trayIcon.setToolTip("[Warning] Inazuma Competitive Terminal");
                } else if (((instance.totalMemory() - instance.freeMemory()) / mbb) < 40) {
                    mb.setText("<html><font color=green>" + mb.getText() + "</font></html>");
                    if (Objects.equals(StatusCB.getSelectedItem(), "OFFLINE")) {
                        trayIcon.setImage(iconoffline);
                        trayIcon.setToolTip("[Offline] Inazuma Competitive Terminal");
                    } else {
                        trayIcon.setImage(icononline);
                        trayIcon.setToolTip("[Online] Inazuma Competitive Terminal");
                    }
                }
                if (delayChannel == 0) {
                    delayChannel = 30;
                    if (!isStartup) {
                        if (Constants.Battery != null) {
                            if (BatteryPercent != batteryStatus.BatteryLifePercent) {
                                BatteryPercent = batteryStatus.BatteryLifePercent;
                                // Battery.getManager().setName("Battery: " + BatteryPercent + "%").queue();
                            }
                        }
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }


    JButton EnableAutoStart = null;
    public JButton getEnableAutoStart() {
        if (EnableAutoStart == null) {
            EnableAutoStart = new JButton(Prefs.isAutoLaunchEnabled ? "Desactivate" : "Activate");
            EnableAutoStart.setBackground(Prefs.isAutoLaunchEnabled ? Color.gray : Color.green);
            EnableAutoStart.addActionListener(e -> {
                if (!Prefs.isAutoLaunchEnabled) {
                    EnableAutoStart.setText("Desactivate");
                    EnableAutoStart.setBackground(Color.gray);
                } else {
                    EnableAutoStart.setText("Activate");
                    EnableAutoStart.setBackground(Color.green);
                }
                Prefs.isAutoLaunchEnabled = !Prefs.isAutoLaunchEnabled;
                Prefs.Save();
                UpdateConsole();
            });
        }
        return EnableAutoStart;
    }
    JButton EnableConsole = null;
    public JButton getEnableConsole() {
        if (EnableConsole == null) {
            EnableConsole = new JButton(Prefs.isAppConsoleEnabled ? "Desactivate" : "Activate");
            EnableConsole.setBackground(Prefs.isAppConsoleEnabled ? Color.gray : Color.green);
            EnableConsole.addActionListener(e -> {
                if (!Prefs.isAppConsoleEnabled) {
                    EnableConsole.setText("Desactivate");
                    EnableConsole.setBackground(Color.gray);
                    System.setOut(new PrintStream(Console));
                    System.setErr(new PrintStream(Console));
                } else {
                    EnableConsole.setText("Activate");
                    EnableConsole.setBackground(Color.green);
                    System.setOut(new PrintStream(oldconsole));
                    System.setErr(new PrintStream(oldconsole));
                }
                Prefs.isAppConsoleEnabled = !Prefs.isAppConsoleEnabled;
                Prefs.Save();
                UpdateConsole();
            });
        }
        return EnableConsole;
    }
    JButton ONOFFButton = null;
    public JButton getONOFFButton() {
        if (ONOFFButton == null) {
            ONOFFButton = new JButton();
            ONOFFButton = new JButton(Prefs.isAutoLaunchEnabled ? "TURN OFF" : "TURN ON");
            ONOFFButton.setBackground(Prefs.isAutoLaunchEnabled ? Color.gray : Color.green);
            ONOFFButton.addActionListener(e -> {
                if (Prefs.isAutoLaunchEnabled) {
                    ONOFFButton.setText("TURN OFF");
                    ONOFFButton.setBackground(Color.gray);
                    DiscordAccount = getBuilder().build();
                } else {
                    ONOFFButton.setText("TURN ON");
                    ONOFFButton.setBackground(Color.green);
                    if (DiscordAccount != null) {
                        DiscordAccount.shutdown();
                        DiscordAccount = null;
                    }
                }
                UpdateConsole();
            });
        }
        return ONOFFButton;
    }
    JButton RateLimits = null;
    public JButton getRateLimitButton() {
        if (RateLimits == null) {
            RateLimits = new JButton();
            RateLimits.addActionListener(e -> {
                System.out.println(DiscordAccount.getRateLimitPool());
                UpdateConsole();
            });
        }
        return RateLimits;
    }


    public JButton getBrowseButton() {
        JButton BrowseFiles = new JButton("Open Folder");
        BrowseFiles.addActionListener(e -> {
            try {
                System.out.println("[Folder] Opening bot data folder.");
                System.out.println("Threads: " + Thread.activeCount());
                UpdateConsole();
                Desktop.getDesktop().open(new File(MainDirectory));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        return BrowseFiles;
    }
    public JButton getSearchUserButton() {
        JButton SearchUser = new JButton("Search User");
        SearchUser.addActionListener(e -> {
            JButton searchButton = new JButton("Search User");
            JTextField searchfield = new JTextField("Enter an ID here...");
            searchButton.addActionListener(E -> {
                try {
                    Desktop.getDesktop().open(new File(MainDirectory + "/user/" + searchfield.getText() + "/"));
                    System.out.println("[Folder] Opening user data folder.");
                    UpdateConsole();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            
            
            JDialog JD = new JDialog();
            JD.setVisible(true);
            JD.setSize(250,100);
            JD.setTitle("Search User");
            JPanel p = new JPanel();
            p.setLayout(new GridLayout(2,1));
            p.add(searchfield);
            p.add(searchButton);
            JD.add(p);
        });
        return SearchUser;
    }
    public JButton getClearConsoleButton() {
        JButton ClearConsole = new JButton("Clear");
        ClearConsole.addActionListener(e -> {
            UpdateConsole();
            Console.reset();
            System.gc();
            System.out.println("[Console] Console has been cleared.");
            UpdateConsole();
        });
        return ClearConsole;
    }
}
