import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

public class DocumentUtils {

	private static Field clone(Field existing) {
		
		
        String name = existing.name();
        
        Number numberValue = existing.numericValue();
        if (numberValue instanceof Integer) {
            return new IntPoint(name, numberValue.intValue());
        } else if (numberValue instanceof Long) {
            return new LongPoint(name, numberValue.longValue());
        } else if (numberValue instanceof Double) {
            return new DoublePoint(name, numberValue.doubleValue());
        }
        
        String stringValue = existing.stringValue();
        if (stringValue != null) {
        	if(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS.equals(existing.fieldType().indexOptions()) || 
        			IndexOptions.DOCS_AND_FREQS_AND_POSITIONS.equals(existing.fieldType().indexOptions()) || 
        			IndexOptions.DOCS_AND_FREQS.equals(existing.fieldType().indexOptions())
        		)
        		return new TextField(name, stringValue, Field.Store.YES);        		
        	else if(DocValuesType.SORTED_SET.equals(existing.fieldType().docValuesType()))
        		return new SortedSetDocValuesFacetField(name, stringValue);
        	
            return new StringField(name, stringValue, Field.Store.YES);
        }
        
        BytesRef bytesRef = existing.binaryValue();
        if (bytesRef != null) {
            // we don't really store any binary fields
            return new StringField(name, bytesRef, Field.Store.YES);
        }
        
        // could be here if field is internal, so ignore
        return null;
    }
	
	// oldDocument ->: existing document
	// fieldsToExclude ->: fields to ignore so that new ones can be created
	public static Document clone(Document oldDocument, String... fieldsToExclude) {
        List<String> excluded = Arrays.asList(fieldsToExclude);

        Document newDocument = new Document();
        oldDocument.getFields().stream().filter((field) -> (!excluded.contains(field.name())))
                .map((field) -> (clone((Field) field))).filter((field)->(field!=null)).forEach(newDocument::add);
        return newDocument;
    }
}

