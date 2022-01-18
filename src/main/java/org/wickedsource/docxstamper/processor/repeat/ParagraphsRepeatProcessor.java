package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.wickedsource.docxstamper.processor.BaseCommentProcessor.getParagraphsInsideComment;
import org.wickedsource.docxstamper.util.CommentUtil;

public abstract class ParagraphsRepeatProcessor extends BaseCommentProcessor {

    protected static class ParagraphsToRepeat {
        List<Object> data;
        List<P> paragraphs;
    }

    protected Map<ParagraphCoordinates, ParagraphsToRepeat> pToRepeat = new HashMap<>();

    protected PlaceholderReplacer<Object> placeholderReplacer;

    protected ParagraphsRepeatProcessor(TypeResolverRegistry typeResolverRegistry) {
        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
    }
    
    protected void processObjectsToAdd(List<Object> objectsToAdd, Object expressionContext, Loop loop) {
    }

    protected void repeatParagraphs(List<Object> objects) {
        ParagraphCoordinates paragraphCoordinates = getCurrentParagraphCoordinates();

        P paragraph = paragraphCoordinates.getParagraph();
        List<P> paragraphs = getParagraphsInsideComment(paragraph);

        ParagraphsToRepeat toRepeat = new ParagraphsToRepeat();
        toRepeat.data = objects;
        toRepeat.paragraphs = paragraphs;

        pToRepeat.put(paragraphCoordinates, toRepeat);
        CommentUtil.deleteComment(getCurrentCommentWrapper());
    }

    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (ParagraphCoordinates rCoords : pToRepeat.keySet()) {
            ParagraphsToRepeat paragraphsToRepeat = pToRepeat.get(rCoords);
            List<Object> expressionContexts = paragraphsToRepeat.data;


            List<Object> objectsToAdd = new ArrayList<>();
            Loop loop = new Loop(0, expressionContexts.size());
            for (final Object expressionContext : expressionContexts) {
                List<Object> exprObjectsToAdd = new ArrayList<>();
                for (P paragraphToClone : paragraphsToRepeat.paragraphs) {
                    P pClone = XmlUtils.deepCopy(paragraphToClone);
                    placeholderReplacer.resolveExpressionsForParagraph(pClone, expressionContext, loop, document);
                    exprObjectsToAdd.add(pClone);
                }
                processObjectsToAdd(exprObjectsToAdd, expressionContext, loop);
                objectsToAdd.addAll(exprObjectsToAdd);
                loop.next();
            }

            Object parent = rCoords.getParagraph().getParent();
            if (parent instanceof ContentAccessor) {
                ContentAccessor contentAccessor = (ContentAccessor) parent;
                int index = contentAccessor.getContent().indexOf(rCoords.getParagraph());
                if (index >= 0) {
                    contentAccessor.getContent().addAll(index, objectsToAdd);
                }

                contentAccessor.getContent().removeAll(paragraphsToRepeat.paragraphs);
            }
        }
    }

    @Override
    public void reset() {
        pToRepeat = new HashMap<>();
    }

}
