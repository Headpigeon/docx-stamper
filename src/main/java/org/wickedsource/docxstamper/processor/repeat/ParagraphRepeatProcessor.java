package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParagraphRepeatProcessor extends BaseCommentProcessor implements IParagraphRepeatProcessor {

    private static class ParagraphsToRepeat {
        List<Object> data;
        List<P> paragraphs;
    }

    private Map<ParagraphCoordinates, ParagraphsToRepeat> pToRepeat = new HashMap<>();

    private PlaceholderReplacer<Object> placeholderReplacer;

    public ParagraphRepeatProcessor(TypeResolverRegistry typeResolverRegistry) {
        this.placeholderReplacer = new PlaceholderReplacer<>(typeResolverRegistry);
    }

    @Override
    public void repeatParagraph(List<Object> objects) {
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


            List<P> paragraphsToAdd = new ArrayList<>();
            Loop loop = new Loop(0, expressionContexts.size());
            for (final Object expressionContext : expressionContexts) {
                for (P paragraphToClone : paragraphsToRepeat.paragraphs) {
                    P pClone = XmlUtils.deepCopy(paragraphToClone);
                    placeholderReplacer.resolveExpressionsForParagraph(pClone, expressionContext, loop, document);

                    paragraphsToAdd.add(pClone);
                }
                loop.next();
            }

            Object parent = rCoords.getParagraph().getParent();
            if (parent instanceof ContentAccessor) {
                ContentAccessor contentAccessor = (ContentAccessor) parent;
                int index = contentAccessor.getContent().indexOf(rCoords.getParagraph());
                if (index >= 0) {
                    contentAccessor.getContent().addAll(index, paragraphsToAdd);
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
