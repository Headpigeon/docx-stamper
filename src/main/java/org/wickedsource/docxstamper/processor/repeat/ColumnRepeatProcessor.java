package org.wickedsource.docxstamper.processor.repeat;

import java.util.List;
import java.util.ListIterator;
import org.docx4j.wml.Br;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;

public class ColumnRepeatProcessor extends ParagraphsRepeatProcessor implements IColumnRepeatProcessor {

    private Logger logger = LoggerFactory.getLogger(ColumnRepeatProcessor.class);
    
    public ColumnRepeatProcessor(TypeResolverRegistry typeResolverRegistry) {
        super(typeResolverRegistry);
    }

    @Override
    public void repeatColumn(List<Object> objects) {
        repeatParagraphs(objects);
    }

//    @Override
//    public void repeatColumn(List<Object> objects, Integer numColumns) {
//        repeatParagraphs(objects, numColumns);
//    }

    @Override
    protected void processObjectsToAdd(List<Object> objectsToAdd, Object expressionContext, Loop loop) {
        if (loop.isLast()) {
            return;
        }
        
        R r = findLastRun(objectsToAdd);
        if (r == null) {
            logger.debug("repeatColumn(): Could not find run for appending column break");
            return;
        }
        
        ObjectFactory docx4jFactory = new ObjectFactory();
        Br colBreak = docx4jFactory.createBr();
        colBreak.setType(STBrType.COLUMN);
        r.getContent().add(colBreak);
    }
    
    public static R findLastRun(List<Object> objects) {
        ListIterator<Object> iter = objects.listIterator(objects.size());
        // Try to find last R in objects to add, possibly inside a P
        while (iter.hasPrevious()) {
            Object obj = iter.previous();
            if (obj instanceof R) {
                return (R)obj;
            } else if (obj instanceof P) {
                P p = (P)obj;
                ListIterator<Object> pIter = p.getContent().listIterator(p.getContent().size());
                while (pIter.hasPrevious()) {
                    Object pObj = pIter.previous();
                    if (pObj instanceof R) {
                        return (R)pObj;
                    }
                }
            }
        }
        return null;
    }

}
