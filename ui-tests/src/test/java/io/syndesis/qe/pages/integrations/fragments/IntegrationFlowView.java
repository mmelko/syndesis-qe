package io.syndesis.qe.pages.integrations.fragments;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.StepFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Slf4j
public class IntegrationFlowView extends SyndesisPageObject {

    private static final class Link {
        public static final By ADD_STEP = By.linkText("Add a step");
        public static final By ADD_CONNECTION = By.linkText("Add a connection");

    }

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-flow-view");

        public static final By NAME = By.cssSelector("input.form-control.integration-name");
        public static final By STEP_ROOT = By.cssSelector("div.flow-view-step");
        public static final By STEP = By.cssSelector("div.parent-step");
        public static final By STEP_TITLE = By.cssSelector("div.step-name.syn-truncate__ellipsis");
        public static final By ACTIVE_STEP_ICON = By.cssSelector("div.icon.active");
        public static final By DELETE = By.className("delete-icon");
        public static final By STEP_INSERT = By.className("step-insert");

        public static final By POPOVER_CLASS = By.className("popover");
        public static final By STEP_DETAILS = By.className("step-details");
        public static final By DATA_WARNING_CLASS = By.className("data-mismatch");

        public static final By FLOW_TITLE = By.cssSelector("h3.flow-view-step-title");


    }

    private static final class Button {
        public static final By Expand = By.cssSelector("button.btn.btn-default.toggle-collapsed.collapsed");
        public static final By Collapse = By.cssSelector("button.btn.btn-default.toggle-collapsed:not(.collapsed)");
    }

    private StepFactory stepComponentFactory = new StepFactory();

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public String getIntegrationName() {
        return this.getRootElement().find(Element.NAME).shouldBe(visible).getAttribute("value");
    }

    /**
     * Check if there's an icon in active state goToNextWizardPhase to the position in the integration flow
     *
     * @param position (start|finish)
     */
    public boolean verifyActivePosition(String position) {
        SelenideElement selenideElement = getRootElement().find(By.cssSelector("div.step." + position.toLowerCase()));
        return selenideElement.find(Element.ACTIVE_STEP_ICON).shouldBe(visible).exists();
    }

    public List<String> getStepsTitlesArray() {
        if (isCollapsed()) {
            $(Button.Expand).click();
        }

        ElementsCollection steps = this.getRootElement().findAll(Element.STEP);

        List<String> stepsArray = new ArrayList<String>();

        for (int i = 0; i < steps.size(); i++) {
            SelenideElement step = steps.get(i);
            SelenideElement title = step.find(Element.STEP_TITLE);
            stepsArray.add(title.getAttribute("title"));
        }
        if (isExpanded()) {
            $(Button.Collapse).click();
        }
        return stepsArray;
    }

    public ElementsCollection getAllTrashes() {
        return this.getRootElement().findAll(Element.DELETE);
    }

    public void clickRandomTrash() {
        this.getElementRandom(Element.DELETE).shouldBe(visible).click();
    }

    public void clickAddStepLink(int pos) {

        List<SelenideElement> allStepInserts = getRootElement().$$(Element.STEP_INSERT)
                .shouldHave(sizeGreaterThanOrEqual(pos));
        SelenideElement stepElement = allStepInserts.get(pos);

        stepElement.shouldBe(visible).hover();

        getRootElement().$(Link.ADD_STEP).shouldBe(visible).click();
    }

    public void clickAddConnectionLink(int pos) {
        List<SelenideElement> allStepInserts = getRootElement().$$(Element.STEP_INSERT)
                .shouldHave(sizeGreaterThanOrEqual(pos));
        SelenideElement stepElement = allStepInserts.get(pos);

        stepElement.scrollIntoView(true).hover();

        getRootElement().$(Link.ADD_CONNECTION).shouldBe(visible).click();
    }

    /**
     * If you only have 3 steps - start, finish and a step in the middle to get the middle step
     * set stepPosition to "middle". Otherwise set it to start or finish to get first/last step.
     *
     * @param stepPosition
     * @return text in popover element
     */
    public String checkTextInHoverTable(String stepPosition) {
        String text;
        if (stepPosition.equalsIgnoreCase("middle")) {
            // This is ugly but for now it works - it has only one usage: for getting our data-mapper step
            // which is between start and finish step.
            // Explanation: we have 3 steps and between them 2 elements with insert step option --> 5 total
            // with class "step" and we want the third element
            $$(By.className("step")).shouldHaveSize(5).get(2)
                    .shouldBe(visible).find(By.className("icon")).shouldBe(visible).hover();
            text = $(By.className("popover")).shouldBe(visible).getText();
        } else {
            $(By.className(stepPosition)).shouldBe(visible).find(By.className("icon")).shouldBe(visible).hover();
            text = $(By.className("popover")).shouldBe(visible).getText();
        }
        return text;
    }

    public String getWarningTextFromStep(int stepPosition) {
        SelenideElement warningIcon = getStepWarningElement(stepPosition).shouldBe(visible);
        //open popup
        warningIcon.click();
        String text = getPopoverText();
        //hide popup
        warningIcon.click();
        return text;
    }

    /**
     * Always follow with shouldBe(visible) as it is needed also for the case when it returns nothing
     *
     * @param stepPosition
     * @return
     */
    public SelenideElement getStepWarningElement(int stepPosition) {
        return getStepOnPosition(stepPosition).$(Element.DATA_WARNING_CLASS);
    }

    public SelenideElement getStepOnPosition(int position) {
        return $$(By.className("step")).shouldBe(sizeGreaterThanOrEqual(position)).get(position).shouldBe(visible);
    }

    public String getPopoverText() {
        String text = $(Element.POPOVER_CLASS).shouldBe(visible).getText();
        log.info("Text found: " + text);
        return text;
    }

    public String getConnectionPropertiesText(SelenideElement connectionStep) {
        return connectionStep.$(Element.STEP_DETAILS).shouldBe(visible).getText();
    }

    public String getFlowTitle() {
        return getRootElement().$(Element.FLOW_TITLE).text();
    }

    public int getNumberOfSteps() {
        return getRootElement().$$(Element.STEP).size();
    }

    public boolean isCollapsed() {
        return $(Button.Expand).exists();
    }

    public boolean isExpanded() {
        return $(Button.Collapse).exists();
    }
}
