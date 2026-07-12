package za.co.fnb.dcre.platform.files;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Declarative fixed-width layout; offsets derive from the field table only
 *  (never from rendered whitespace, register provenance rule). */
public final class FixedWidthLayout {

    private final Map<String, int[]> offsets = new LinkedHashMap<>();
    private final int length;

    public FixedWidthLayout(List<LayoutField> fields) {
        int pos = 0;
        for (LayoutField f : fields) {
            offsets.put(f.name(), new int[]{pos, pos + f.width()});
            pos += f.width();
        }
        this.length = pos;
    }

    public String slice(String line, String field) {
        int[] o = offsets.get(field);
        if (o == null) {
            throw new IllegalArgumentException("unknown field " + field);
        }
        return line.substring(o[0], o[1]);
    }

    public int length() {
        return length;
    }
}
