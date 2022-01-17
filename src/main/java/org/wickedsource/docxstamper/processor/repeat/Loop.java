package org.wickedsource.docxstamper.processor.repeat;

/**
 *
 * @author Thierry
 */
public class Loop {

    private long current;
    private long total;

    public Loop(long index, long count) {
        this.current = index;
        this.total = count;
    }
    
    public void next() {
        this.current++;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public boolean isFirst() {
        return (current == 0);
    }
    
    public boolean isLast() {
        return (current == total - 1);
    }
    
}
