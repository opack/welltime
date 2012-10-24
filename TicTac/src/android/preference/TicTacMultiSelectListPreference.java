package android.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

/**
 * 
 * @author declanshanaghy
 * http://blog.350nice.com/wp/archives/240
 * MultiChoice Preference Widget for Android
 *
 * @contributor matiboy
 * Added support for check all/none and custom separator defined in XML.
 * IMPORTANT: The following attributes MUST be defined (probably inside attr.xml) for the code to even compile
 * <declare-styleable name="ListPreferenceMultiSelect">
    	<attr format="string" name="checkAll" />
    	<attr format="string" name="separator" />
    </declare-styleable>
 *  Whether you decide to then use those attributes is up to you.
 *
 */
public class TicTacMultiSelectListPreference extends ListPreference {
	public static final String SEPARATOR = "#"; 
	private boolean[] mClickedDialogEntryIndices;
	
	// Constructor
	public TicTacMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
     // Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[getEntries().length];
    }
	
	@Override
    public void setEntries(CharSequence[] entries) {
    	super.setEntries(entries);
    	// Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[entries.length];
    }
    
    public TicTacMultiSelectListPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
    	CharSequence[] entries = getEntries();
    	CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, 
                new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean val) {
						mClickedDialogEntryIndices[which] = val;
					}
        });
    }
    
    public String[] parseStoredValue(CharSequence val) {
		if ( "".equals(val) ) {
			return null;
		}
		else {
			return ((String)val).split(SEPARATOR);
		}
    }
    
    private void restoreCheckedEntries() {
    	CharSequence[] entryValues = getEntryValues();
    	
    	// Explode the string read in sharedpreferences
    	String[] vals = parseStoredValue(getValue());
    	
    	if ( vals != null ) {
    		List<String> valuesList = Arrays.asList(vals);
//        	for ( int j=0; j<vals.length; j++ ) {
//    		TODO: Check why the trimming... Can there be some random spaces added somehow? What if we want a value with trailing spaces, is that an issue?
//        		String val = vals[j].trim();
        	for ( int i=0; i<entryValues.length; i++ ) {
        		CharSequence entry = entryValues[i];
            	if ( valuesList.contains(entry) ) {
        			mClickedDialogEntryIndices[i] = true;
        		}
        	}
//        	}
    	}
    }

	@Override
    protected void onDialogClosed(boolean positiveResult) {
//        super.onDialogClosed(positiveResult);
		ArrayList<String> values = new ArrayList<String>();
        
    	CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
        	for ( int i=0; i<entryValues.length; i++ ) {
        		if ( mClickedDialogEntryIndices[i] == true ) {
        			// Don't save the state of check all option - if any
        			String val = (String) entryValues[i];
    				values.add(val);
        		}
        	}

        	final String newValue = join(values, SEPARATOR);
            if (callChangeListener(newValue)) {
        		setValue(newValue);
            }
        }
    }
	
	// Credits to kurellajunior on this post http://snippets.dzone.com/posts/show/91
	protected static String join( Iterable< ? extends Object > pColl, String separator )
    {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
            return "";
        StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
        while ( oIter.hasNext() )
            oBuilder.append( separator ).append( oIter.next() );
        return oBuilder.toString();
    }
	
	// TODO: Would like to keep this static but separator then needs to be put in by hand or use default separator "OV=I=XseparatorX=I=VO"...
	/**
	 * 
	 * @param straw String to be found
	 * @param haystack Raw string that can be read direct from preferences
	 * @param separator Separator string. If null, static default separator will be used
	 * @return boolean True if the straw was found in the haystack
	 */
	public static boolean contains( String straw, String haystack){
		String[] vals = haystack.split(SEPARATOR);
		for( int i=0; i<vals.length; i++){
			if(vals[i].equals(straw)){
				return true;
			}
		}
		return false;
	}
}