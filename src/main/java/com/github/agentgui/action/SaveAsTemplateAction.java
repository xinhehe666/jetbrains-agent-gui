package com.github.agentgui.action;

import com.github.agentgui.i18n.AgentGuiBundle;
import com.github.agentgui.model.SessionTemplate;
import com.github.agentgui.session.ClaudeSession;
import com.github.agentgui.settings.SessionTemplateService;
import com.github.agentgui.ui.toolwindow.ClaudeChatWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Action to save the current session configuration as a reusable template.
 */
public class SaveAsTemplateAction extends ChatToolWindowAction {

    private static final Logger LOG = Logger.getInstance(SaveAsTemplateAction.class);

    public SaveAsTemplateAction() {
        super();
        getTemplatePresentation().setText(AgentGuiBundle.message("action.saveAsTemplate.text"));
        getTemplatePresentation().setDescription(AgentGuiBundle.message("action.saveAsTemplate.description"));
    }

    @Override
    protected void performAction(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ClaudeChatWindow chatWindow) {
        ClaudeSession session = chatWindow.getSession();
        if (session == null) {
            handleNoActiveSession(project);
            return;
        }

        // Get template name from user
        String templateName = showInputDialog(
                project,
                AgentGuiBundle.message("template.save.name.prompt"),
                AgentGuiBundle.message("template.save.name.title")
        );

        if (templateName == null || templateName.trim().isEmpty()) {
            return; // User cancelled
        }

        templateName = templateName.trim();

        // Check if template already exists
        SessionTemplateService templateService = SessionTemplateService.getInstance();
        if (templateService.templateExists(templateName)) {
            int result = showOverwriteDialog(
                    project,
                    AgentGuiBundle.message("template.save.overwrite.prompt", templateName),
                    AgentGuiBundle.message("template.save.overwrite.title")
            );
            if (result != Messages.YES) {
                return; // User chose not to overwrite
            }
        }

        // Create template from current session state
        SessionTemplate template = new SessionTemplate(
            templateName,
            session.getProvider(),
            session.getModel(),
            session.getPermissionMode(),
            session.getReasoningEffort(),
            session.getCwd(),
            session.getState().isPsiContextEnabled()
        );

        // Save template
        templateService.saveTemplate(template);

        showInfoMessage(
                project,
                AgentGuiBundle.message("template.save.success", templateName),
                AgentGuiBundle.message("template.save.success.title")
        );

        LOG.info("Saved session template: " + templateName);
    }

    void handleNoActiveSession(Project project) {
        showErrorDialog(
                project,
                AgentGuiBundle.message("template.save.error.noSession"),
                AgentGuiBundle.message("template.save.error.title")
        );
    }

    protected void showErrorDialog(Project project, String message, String title) {
        Messages.showErrorDialog(project, message, title);
    }

    protected String showInputDialog(Project project, String prompt, String title) {
        return Messages.showInputDialog(project, prompt, title, Messages.getQuestionIcon(), "", null);
    }

    protected int showOverwriteDialog(Project project, String prompt, String title) {
        return Messages.showYesNoDialog(project, prompt, title, Messages.getQuestionIcon());
    }

    protected void showInfoMessage(Project project, String message, String title) {
        Messages.showInfoMessage(project, message, title);
    }
}
