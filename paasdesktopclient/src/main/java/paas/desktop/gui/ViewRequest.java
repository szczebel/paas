package paas.desktop.gui;

public enum ViewRequest {
    LOGIN {
        @Override
        void visit(MainFrameOverlay mainFrame) {
            mainFrame.showLogin();
        }
    },
    REGISTRATION {
        @Override
        void visit(MainFrameOverlay mainFrame) {
            mainFrame.showRegistration();
        }
    },
    NEW_VERSION {
        @Override
        void visit(MainFrameOverlay mainFrame) {
            mainFrame.tellUserAboutNewVersion();
        }
    };

    abstract void visit(MainFrameOverlay mainFrame);
}
