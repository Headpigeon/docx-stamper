package org.wickedsource.docxstamper.processor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.api.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.api.coordinates.RunCoordinates;
import org.wickedsource.docxstamper.util.CommentWrapper;

import java.util.Objects;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;

public abstract class BaseCommentProcessor implements ICommentProcessor {

	private ParagraphCoordinates currentParagraphCoordinates;

	private RunCoordinates currentRunCoordinates;

	private CommentWrapper currentCommentWrapper;
    
    private Map<Class<?>, Object> proxyInterfaceImplementations = new HashMap<>();
    

	public RunCoordinates getCurrentRunCoordinates() {
		return currentRunCoordinates;
	}

	@Override
	public void setCurrentRunCoordinates(RunCoordinates currentRunCoordinates) {
		this.currentRunCoordinates = currentRunCoordinates;
	}

	@Override
	public void setCurrentParagraphCoordinates(ParagraphCoordinates coordinates) {
		this.currentParagraphCoordinates = coordinates;
	}

	public ParagraphCoordinates getCurrentParagraphCoordinates() {
		return currentParagraphCoordinates;
	}

    public Map<Class<?>, Object> getProxyInterfaceImplementations() {
        return proxyInterfaceImplementations;
    }

    public void setProxyInterfaceImplementations(Map<Class<?>, Object> proxyInterfaceImplementations) {
        this.proxyInterfaceImplementations = proxyInterfaceImplementations;
    }
    
	@Override
	public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
		this.currentCommentWrapper = currentCommentWrapper;
	}

	public CommentWrapper getCurrentCommentWrapper() {
		return currentCommentWrapper;
	}

    public static List<P> getParagraphsInsideComment(P paragraph) {
        BigInteger commentId = null;
        boolean foundEnd = false;

        List<P> paragraphs = new ArrayList<>();
        paragraphs.add(paragraph);

        for (Object object : paragraph.getContent()) {
            if (object instanceof CommentRangeStart) {
                commentId = ((CommentRangeStart) object).getId();
            }
            if (object instanceof CommentRangeEnd && commentId != null && commentId.equals(((CommentRangeEnd) object).getId())) {
                foundEnd = true;
            }
        }
        if (!foundEnd && commentId != null) {
            Object parent = paragraph.getParent();
            if (parent instanceof ContentAccessor) {
                ContentAccessor contentAccessor = (ContentAccessor) parent;
                int index = contentAccessor.getContent().indexOf(paragraph);
                for (int i = index + 1; i < contentAccessor.getContent().size() && !foundEnd; i ++) {
                    Object next = contentAccessor.getContent().get(i);

                    if (next instanceof CommentRangeEnd && ((CommentRangeEnd) next).getId().equals(commentId)) {
                        foundEnd = true;
                    } else {
                        if (next instanceof P) {
                            paragraphs.add((P) next);
                        }
                        if (next instanceof ContentAccessor) {
                            ContentAccessor childContent = (ContentAccessor) next;
                            for (Object child : childContent.getContent()) {
                                if (child instanceof CommentRangeEnd && ((CommentRangeEnd) child).getId().equals(commentId)) {
                                    foundEnd = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return paragraphs;
    }

}
