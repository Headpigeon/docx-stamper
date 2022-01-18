package org.wickedsource.docxstamper.processor.repeat;

import java.util.List;
import org.wickedsource.docxstamper.api.typeresolver.TypeResolverRegistry;

public class ParagraphRepeatProcessor extends ParagraphsRepeatProcessor implements IParagraphRepeatProcessor {

    public ParagraphRepeatProcessor(TypeResolverRegistry typeResolverRegistry) {
        super(typeResolverRegistry);
    }

    @Override
    public void repeatParagraph(List<Object> objects) {
        repeatParagraphs(objects);
    }
    
}
