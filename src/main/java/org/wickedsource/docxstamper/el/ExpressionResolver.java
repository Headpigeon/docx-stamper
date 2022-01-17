package org.wickedsource.docxstamper.el;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.processor.repeat.Loop;

public class ExpressionResolver {

    private static final ExpressionUtil expressionUtil = new ExpressionUtil();

    private final EvaluationContextConfigurer evaluationContextConfigurer;

    public ExpressionResolver() {
        this.evaluationContextConfigurer = new NoOpEvaluationContextConfigurer();
    }

    public ExpressionResolver(EvaluationContextConfigurer evaluationContextConfigurer) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
    }

    /**
     * Runs the given expression against the given context object and returns the result of the evaluated expression.
     *
     * @param expressionString the expression to evaluate.
     * @param contextRoot      the context object against which the expression is evaluated.
     * @return the result of the evaluated expression.
     */
    public Object resolveExpression(String expressionString, Object contextRoot) {
        return resolveExpression(expressionString, contextRoot, null);
    }
    
    /**
     * Runs the given expression against the given context object and returns the result of the evaluated expression.
     *
     * @param expressionString the expression to evaluate.
     * @param contextRoot      the context object against which the expression is evaluated.
     * @param loop             information about the current iteration step. null if not applicable.
     * @return the result of the evaluated expression.
     */
    public Object resolveExpression(String expressionString, Object contextRoot, Loop loop) {
        if ((expressionString.startsWith("${") || expressionString.startsWith("#{")) && expressionString.endsWith("}")) {
            expressionString = expressionUtil.stripExpression(expressionString);
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextRoot);
        if (loop != null) {
            evaluationContext.setVariable("loopCurrent", loop.getCurrent());
            evaluationContext.setVariable("loopTotal", loop.getTotal());
            evaluationContext.setVariable("loopFirst", loop.isFirst());
            evaluationContext.setVariable("loopLast", loop.isLast());
        }
        evaluationContextConfigurer.configureEvaluationContext(evaluationContext);
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext);
    }

}
