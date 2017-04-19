package paas.desktop.gui.infra;

import org.icepdf.ri.common.SwingController;
import org.springframework.stereotype.Component;

@Component
public class UserGuide extends SwingController {
    {
        openDocument(getClass().getResourceAsStream("/manual.pdf"), "description", "path/url");
    }
}
