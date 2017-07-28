package de.toem.impulse.disclosure.examples;

import java.util.List;

import de.toem.impulse.disclosures.AbstractReadableDisclosure;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.pattern.element.ICell;

public class ExampleShortestEventDisclosure extends AbstractReadableDisclosure {

    @Override
    public int getFormats() {
        return FORMAT_STRING | FORMAT_INTEGER;
    }

    @Override
    protected Object extractData(ICell cell, IReadableSamples readable, Object properties) {

        if (readable != null && readable.ensureSettled() && readable.getCount() >= 2) {
            long shortest = Long.MAX_VALUE;
            long ppos = readable.getStartUnits();
            for (int n = 1; n < readable.getCount(); n++) {
                long pos = readable.unitsAt(n);
                long diff = pos - ppos;
                if (diff < shortest)
                    shortest = diff;
                ppos = pos;
            }
            return shortest;
        }
        return null;
    }

    @Override
    protected boolean combineReadables() {
        return true;
    }

    @Override
    protected Object combineData(ICell cell, List<Object> disclosedChildren, Object properties) {

        long min = Long.MAX_VALUE;
        for (Object data : disclosedChildren) {
            if (data instanceof Long) {
                min = Math.min(min, (Long) data);
            }
        }
        return min!=Long.MAX_VALUE?min:null;
    }

    @Override
    protected Object format(Object data, int format) {
        if ((format & FORMAT_STRING) != 0) {
            return String.valueOf(data);
        }
        if ((format & FORMAT_INTEGER) != 0) {
            return data;
        }
        return null;
    }

}