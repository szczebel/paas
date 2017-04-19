package paas.desktop.gui.views;

import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.UserGuide;
import swingutils.components.LazyInitRichAbstractView;

import javax.swing.*;

import static org.icepdf.ri.common.views.DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION;
import static swingutils.components.ComponentFactory.decorate;
import static swingutils.layout.LayoutBuilders.borderLayout;
import static swingutils.layout.LayoutBuilders.hBox;


@Component
public class DocsView extends LazyInitRichAbstractView {

    @Autowired
    private UserGuide userGuide;

    @Override
    protected JComponent wireAndLayout() {
        SwingViewBuilder b = new SwingViewBuilder(userGuide);
        JComponent centerPanel = borderLayout()
                .north(
                        hBox(4,
                                b.buildPageNavigationToolBar(),
                                b.buildZoomToolBar(),
                                b.buildFitWidthButton()
                        )
                )
                .center((JComponent) userGuide.getDocumentViewController().getViewContainer())
                .build();
        centerPanel = decorate(centerPanel).withTitledSeparator("User Guide").withEmptyBorder(0, 4, 0, 0).get();


        JComponent searchPanel = b.buildSearchPanel();
        ((JComponent) searchPanel.getComponent(0)).setBorder(null);
        searchPanel = decorate(searchPanel).withTitledSeparator("Search").get();

        userGuide.getDocumentViewController().setFitMode(DocumentViewController.PAGE_FIT_WINDOW_WIDTH);
        userGuide.getDocumentViewController().setToolMode(DISPLAY_TOOL_TEXT_SELECTION);
        userGuide.setPageViewMode(DocumentViewControllerImpl.ONE_COLUMN_VIEW, false);
        userGuide.showPage(0);


        JComponent all = borderLayout().west(searchPanel).center(centerPanel).build();
        return decorate(all)
                .withBorder(null)
                .withEmptyBorder(0, 4, 4, 4)
                .get();
    }
}
