package paas.desktop.gui;

public enum ViewRequest {
    LOGIN {
        @Override
        void visit(MainFrame mainFrame) {
            mainFrame.showLogin();
        }
    },
    REGISTRATION {
        @Override
        void visit(MainFrame mainFrame) {
            mainFrame.showRegistration();
        }
    },
    NEW_VERSION {
        @Override
        void visit(MainFrame mainFrame) {
            mainFrame.tellUserAboutNewVersion();
        }
    };

    abstract void visit(MainFrame mainFrame);
}
