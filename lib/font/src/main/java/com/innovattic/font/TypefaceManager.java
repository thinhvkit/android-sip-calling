package com.innovattic.font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.widget.TextView;

public class TypefaceManager
{
	
	private static final String TAG = TypefaceManager.class.getSimpleName();
	private static TypefaceManager INSTANCE;
	
	// Different tags used in XML file.
	private static final String TAG_FAMILY = "family";
	private static final String TAG_NAMESET = "nameset";
	private static final String TAG_NAME = "name";
	private static final String TAG_FILESET = "fileset";
	private static final String TAG_FILE = "file";
	
	private static final byte INVALID     = -1;
	private static final byte NONE        = 0;
	private static final byte REGULAR     = 1;
	private static final byte BOLD        = 2;
	private static final byte ITALIC      = 4;
	private static final byte BOLD_ITALIC = 8;
	
	/** The style map, used to map a missing style to another style. */
	private static final Map<Byte, List<Byte>> styleMap;
	/** The Typeface cache, keyed by their asset file name. */
	private static final Map<String, Typeface> mCache = new HashMap<String, Typeface>();
	
	private final Context mContext;
	/** The xml file in which the fonts are defined. */
	private final int mXmlResource;
	/** The mapping of font names to Font objects as defined in the xml file. */
	private final Map<String, Font> mFonts;
	
	static
	{
		// Initialize the style map
		// The style map is used to determine which font style must be used if
		// the requested style is not available.
		styleMap = new HashMap<Byte, List<Byte>>();
		styleMap.put(REGULAR, Arrays.asList(BOLD, ITALIC, BOLD_ITALIC));
		styleMap.put(BOLD, Arrays.asList(REGULAR, BOLD_ITALIC, ITALIC));
		styleMap.put(ITALIC, Arrays.asList(REGULAR, BOLD_ITALIC, BOLD));
		styleMap.put(BOLD_ITALIC, Arrays.asList(BOLD, ITALIC, REGULAR));
	}
	
	private static class Font
	{
		public List<String> names = new ArrayList<String>();
		public Map<Byte, String> styles = new HashMap<Byte, String>();
	}
	
	/**
	 * Initializes the singleton instance of the TypefaceManager. It will read
	 * the given xml font file pointed to by xmlResource, but doesn't yet load
	 * the fonts.
	 */
	public static synchronized void initialize(Context context, int xmlResource)
	{
		if (INSTANCE == null)
			INSTANCE = new TypefaceManager(context, xmlResource);
		if (INSTANCE.mXmlResource != xmlResource)
			Log.w(TAG, "Singleton instance of TypefaceManager was initialized" +
				" with a different xml font file previously. " +
				"Re-initialization will not occur.");
		if (!INSTANCE.mContext.equals(context))
			Log.w(TAG, "Singleton instance of TypefaceManager was initialized" +
				" with a different context previously. Re-initialization will" +
				" not occur.");
	}
	
	/**
	 * Returns the singleton instance of the TypefaceManager. It will throw an
	 * exception if it is called before {@link #initialize(Context, int)
	 * initialization}.
	 * 
	 * @return The TypefaceManager
	 */
	public static TypefaceManager getInstance()
	{
		if (INSTANCE == null)
			throw new IllegalStateException("Cannot use " +
				"TypefaceManager.getInstance() before it is initialized. Use " +
				"TypefaceManager.initialize(Context, int) to initialize the " +
				"TypefaceManager.");
		return INSTANCE;
	}
	
	/**
	 * Initializes the typeface manager with the given xml font file.
	 * 
	 * @param context A context with which the xml font file and the assets can
	 *        be accessed.
	 * @param xmlResource The resource id of an xml file that specifies the
	 *        fonts that are present in the assets.
	 * @return The number of unique font files in the xml file that exist. The
	 *         font files are not yet opened or verified, it only means that
	 *         they exist.
	 */
	private TypefaceManager(Context context, int xmlResource)
	{
		mFonts = new HashMap<String, Font>();
		this.mXmlResource = xmlResource;
		mContext = context;
		parse();
	}
	
	/**
	 * Returns the typeface identified by the given font name and style. The
	 * font must be a name defined in any nameset in the font xml file. The
	 * style must be one of the constants defined by {@link Typeface}.
	 * 
	 * @param name
	 * @return
	 */
	public Typeface getTypeface(String name)
	{
		return getTypeface(name, Typeface.NORMAL);
	}
	
	/**
	 * Returns the typeface identified by the given font name and style. The
	 * font must be a name defined in any nameset in the font xml file. The
	 * style must be one of the constants defined by {@link Typeface}.
	 * 
	 * @param name
	 * @param style
	 * @return
	 */
	public Typeface getTypeface(String name, int style)
	{
		Font font = mFonts.get(name.toLowerCase());
		String file = font.styles.get(toInternalStyle(style));
		synchronized (mCache)
		{
			if (!mCache.containsKey(file))
			{
				Log.i(TAG, String.format("Inflating font %s (style %d) with " +
					"file %s", name, style, file));
				Typeface t = Typeface.createFromAsset(mContext.getAssets(),
					String.format("fonts/%s", file));
				mCache.put(file, t);
			}
		}
		return mCache.get(font.styles.get(toInternalStyle(style)));
	}
	
	/**
	 * Convenience method to set the typeface of the target view to the font
	 * identified by fontName. The text style that will be used is {@code
	 * Typeface#NORMAL}. Returns false if the font wasn't found or couldn't be
	 * loaded, returns true otherwise.
	 * 
	 * @param target A TextView in which the font must be used.
	 * @param fontName The name of the font. Must match a name defined in a name
	 *        tag in the xml font file.
	 * @return {@code true} if the font could was set in the target, {@code
	 *         false} otherwise.
	 */
	public boolean setTypeface(TextView target, String fontName)
	{
		return setTypeface(target, fontName, Typeface.NORMAL);
	}

	/**
	 * Set the text style of the given TextView. The text style must be one of the constants defined
	 * in {@link Typeface}. If the target had a custom font applied (as specified in the {@link
	 * ExtraFontData}), that font will be used to find the appropriate text style.
	 *
	 * @param target A TextView in which the text style must be set.
	 * @param textStyle A text style: {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link
	 *        Typeface#ITALIC} or {@link Typeface#BOLD_ITALIC}.
	 * @return {@code true} if the text style was set in the target, {@code false} otherwise.
	 */
	public boolean setTextStyle(TextView target, int textStyle)
	{
		ExtraFontData data = getFontData(target);
		data.style = textStyle;
		if (data.font == null) {
			// Default Android font
			target.setTypeface(target.getTypeface(), textStyle);
			return true;
		} else {
			// Custom font
			return setTypeface(target, data.font, textStyle);
		}
	}
	
	/**
	 * Convenience method to set the typeface of the target view to the font
	 * identified by fontName using the textStyle. The textStyle must be one of
	 * the constants defined by {@code Typeface}. Returns false if the font
	 * wasn't found or couldn't be loaded, returns true otherwise.
	 * 
	 * @param target A TextView in which the font must be used.
	 * @param fontName The name of the font. Must match a name defined in a name
	 *        tag in the xml font file.
	 * @param textStyle A text style: normal, bold, italic or bold_italic.
	 * @return {@code true} if the font could was set in the target, {@code
	 *         false} otherwise.
	 */
	public boolean setTypeface(TextView target, String fontName, int textStyle)
	{
		Typeface tf = null;
		try
		{
			tf = getTypeface(fontName, textStyle);
			// If the font was retrieved successfully, store it in the data.
			ExtraFontData data = getFontData(target);
			data.font = fontName;
			data.style = textStyle;
		}
		catch (Exception e)
		{
			Log.e(TAG, "Could not get typeface "+fontName+" with style "+textStyle);
			return false;
		}

		target.setTypeface(tf);
		return true;
	}
	
	/**
	 * Parses the xml font file. After this method, {@link #mFonts} will contain
	 * all fonts encountered in the xml font file for which at least one of the
	 * defined font file(s) exist. 
	 */
	private void parse()
	{
		XmlResourceParser parser = null;
		try
		{
			String[] fontAssets = getAvailableFontFiles();
			parser = mContext.getResources().getXml(mXmlResource);
			
			String tag;
			Font font = null;
			byte style = INVALID;
			boolean isName = false;
			boolean isFile = false;
			int eventType = parser.getEventType();
			
			do
			{
				tag = parser.getName();
				switch (eventType)
				{
					case XmlPullParser.START_TAG:
						// One of the font-families.
						if (tag.equals(TAG_FAMILY))
							font = new Font();
						else if (tag.equals(TAG_NAMESET))
							; // nothing to do for namesets
						else if (tag.equals(TAG_FILESET))
							style = NONE;
						// A name that maps to this font-family.
						else if (tag.equals(TAG_NAME))
							isName = true;
						// A font file to be used for this font-family.
						else if (tag.equals(TAG_FILE))
							isFile = true;
						break;
						
					case XmlPullParser.END_TAG:
						if (tag.equals(TAG_FAMILY))
						{
							// Family is fully defined, process it.
							// Add all missing style mappings
							addMissingStyles(font);
							// Add all the font names to the lookup tbl,
							// but only if any font files were defined.
							if (!font.styles.isEmpty())
								for (String name : font.names)
									// Don't override fonts (as defined by the
									if (!mFonts.containsKey(name))
										mFonts.put(name, font);
							// And reset the font for the next family
							font = null;
						}
						// Done reading a name for this family.
						else if (tag.equals(TAG_NAME))
							isName = false;
						// Done reading a font file for this family.
						else if (tag.equals(TAG_FILE))
							isFile = false;
						else if (tag.equals(TAG_FILESET))
							style = INVALID;
						break;
						
					case XmlPullParser.TEXT:
						String text = parser.getText();
						if (isName)
						{
							// Value is a font name
							font.names.add(text.toLowerCase());
						}
						else if (isFile)
						{
							// Value is a font file
							String ttf = text;
							// Determine which style file this is
							style = next(style);
							// Check if the file exists
							if (Arrays.binarySearch(fontAssets, ttf) < 0)
								Log.w(TAG, "Couldn't find font in the assets: "+
									ttf);
							// Add the style
							else
								font.styles.put(style, ttf);
						}
				}
				eventType = parser.next();
				
			} while (eventType != XmlPullParser.END_DOCUMENT);

		}
		catch (XmlPullParserException e)
		{
			throw new InflateException("Error inflating font XML", e);
		}
		catch (IOException e)
		{
			throw new InflateException("Error inflating font XML", e);
		}
		finally
		{
			if (parser != null)
				parser.close();
		}
	}
	
	/**
	 * Returns the style that comes after the given style in a fileset in the
	 * font xml file. This order is defined by Android in the definition of the
	 * system fonts and vendor fonts (see https://github.com/android/platform_frameworks_base/blob/master/data/fonts/vendor_fonts.xml).
	 * 
	 * @param style
	 * @return
	 */
	private static byte next(byte style)
	{
		switch (style)
		{
			case NONE:     return REGULAR;
			case REGULAR:  return BOLD;
			case BOLD:     return ITALIC;
			case ITALIC:   return BOLD_ITALIC;
			default:       return INVALID;
		}
	}
	
	/**
	 * Converts a style constant from {@link Typeface} to a style constant from
	 * the {@link TypefaceManager}.
	 * 
	 * @param typefaceStyle
	 * @return
	 */
	private static byte toInternalStyle(int typefaceStyle)
	{
		switch (typefaceStyle)
		{
			case Typeface.NORMAL:      return REGULAR;
			case Typeface.BOLD:        return BOLD;
			case Typeface.ITALIC:      return ITALIC;
			case Typeface.BOLD_ITALIC: return BOLD_ITALIC;
			default:                   return INVALID;
		}
	}
	
	/**
	 * Adds style mappings for all styles that are not loaded in the given font.
	 * A font may be defined without all four styles regular, bold, italic and
	 * bold-italic. In that case, the missing styles will be mapped to the most
	 * preferred style that is present.
	 * 
	 * @param font
	 */
	private static void addMissingStyles(Font font)
	{
		byte availableStyles = 0;
		Map<Byte, String> styles = font.styles;
		for (byte style : styles.keySet())
			availableStyles |= style;
		
		for (byte style : styleMap.keySet())
			if (isMissing(style, availableStyles))
				for (byte replacement : styleMap.get(style))
					if (!isMissing(replacement, availableStyles))
					{
						styles.put(style, styles.get(replacement));
						break;
					}
	}
	
	/**
	 * Returns a list of all file names in the asset folder "fonts", recursively.
	 * 
	 * @return
	 */
	private String[] getAvailableFontFiles()
	{
		// Note that empty directories are filtered during the build process. Thus, when listing
		// assets of a certain path, an empty list means the path is a file and a non-empty list
		// means the path is a directory.
		try
		{
			final String ROOT = "fonts";
			final String SLASH = File.separator;

			// Initialize variables
			final List<String> fontAssetList = new ArrayList<String>();
			final AssetManager assets = mContext.getAssets();
			final Queue<String> q = new LinkedList<String>();

			// Recursively find all files (using bfs to avoid recursive calls)
			String[] list = assets.list(ROOT);
			addToQueue(q, list, "");
			while (!q.isEmpty()) {
				final String path = q.remove();
				// file or dir?
				list = assets.list(ROOT + SLASH + path);
				if (list.length == 0) {
					// File (therefor, a potential font)
					fontAssetList.add(path);
				} else {
					// Dir (therefor, potentially contains fonts)
					addToQueue(q, list, path + SLASH);
				}
			}

			// Post process all found files
			String[] fontAssets = fontAssetList.toArray(new String[fontAssetList.size()]);
			Arrays.sort(fontAssets);
			return fontAssets;
		}
		catch (IOException e)
		{
			Log.e(TAG, "Couldn't access assets; fonts are not available");
			return new String[0];
		}
	}

	/**
	 * Adds the given paths to the queue, prefixing them with their origin.
	 *
	 * @param q The queue to which to add the paths
	 * @param paths A list of paths that will be added to the queue
	 * @param relativeTo The origin to which the paths are relative. Must include the file separator
	 *        ({@link File#separator}) at the end.
	 */
	private void addToQueue(final Queue<String> q, final String[] paths, final String relativeTo)
	{
		for (String child : paths) {
			q.add(relativeTo + child);
		}
	}

	/**
	 * Returns if all set bits in the needle are not set in the haystack. In
	 * other words, if the haystack is missing all the bits from the needle,
	 * this returns {@code true}.
	 * 
	 * @param needle All bits set to 1 in this byte will be checked.
	 * @param haystack The bits in this byte will be checked.
	 * @return {@code true} iff all bits that need to be checked in the
	 *         haystack are 0.
	 */
	private static boolean isMissing(byte needle, byte haystack)
	{
		return (haystack&needle) == 0;
	}
	
	/**
	 * Applies the font and all related custom properties found in the
	 * attributes of the given AttributeSet or the default style to the given
	 * target. Typically, the AttributeSet consists of the attributes contained
	 * in the xml tag that defined the target. The target can be any TextView or
	 * subclass thereof. The read properties will be stored in an {@link
	 * ExtraFontData} instance stored as a tag with id {@link
	 * R.id#flFontsExtraData} in the target view. If an instance was already set
	 * as a tag in the target view, it will be reused. All encountered
	 * properties are overridden. The properties in the data holder can be
	 * changed later, but it will depend on the nature of the property whether
	 * or not this change will take effect. Properties that are applied at
	 * initialization will not be applied when changed and properties that are
	 * applied during the render cycle will be applied when changed.
	 * 
	 * @param target A TextView, or any UI element that inherits from TextView.
	 * @param attrs The attributes from the xml tag that defined the target.
	 * @param defStyle The style that is applied to the target element. This may
     *        either be an attribute resource, whose value will be retrieved
     *        from the current theme, or an explicit style resource.
	 */
	public static void applyFont(TextView target, AttributeSet attrs, int defStyle)
	{
		if (target == null || target.isInEditMode())
			return;
		ExtraFontData data = getFontData(target);
		
		// First get the font attribute from the textAppearance:
		Theme theme = target.getContext().getTheme();
		// Get the text appearance that's currently in use
		TypedArray a = theme.obtainStyledAttributes(attrs,
			new int[] {android.R.attr.textAppearance}, defStyle, 0);
		int textAppearanceStyle = -1;
		try {
			textAppearanceStyle = a.getResourceId(0, -1);
		} finally {
			a.recycle();
		}
		// Get the font and style defined in the text appearance
		TypedArray appearance = null;
		if (textAppearanceStyle != -1)
			appearance = theme.obtainStyledAttributes(textAppearanceStyle,
				R.styleable.Fonts);
		getAttributes(appearance, data);
		
		// Then get the font attribute from the FontTextView itself:
		a = theme.obtainStyledAttributes(attrs, R.styleable.Fonts, defStyle, 0);
		getAttributes(a, data);
		
		// Now we have the font, apply it
		if (data.font != null) {
			getInstance().setTypeface(target, data.font, data.style);
		}
	}
	
	/**
	 * Fetches the font attributes from the given typed array and overrides all
	 * properties in the given data holder that are present in the typed array.
	 * 
	 * @param a A TypedArray from which the attributes will be fetched. It will
	 *        be recycled if not null.
	 * @param data The data holder in which all read properties are stored.
	 */
	private static void getAttributes(final TypedArray a, final ExtraFontData data)
	{
		if (a == null)
			return;
		try
		{
			// Iterate over all attributes in 'Android-style'
			// (similar to the implementation of the TextView constructor)
			int n = a.getIndexCount();
			for (int i = 0; i < n; i++)
			{
				int attr = a.getIndex(i);
				if (attr == R.styleable.Fonts_flFont) {
					data.font = a.getString(attr);
				} else if (attr == R.styleable.Fonts_android_textStyle) {
					data.style = a.getInt(attr, Typeface.NORMAL);
				} else if (attr == R.styleable.Fonts_flBorderWidth) {
					data.borderWidth = a.getDimensionPixelSize(attr, 0);
				} else if (attr == R.styleable.Fonts_flBorderColor) {
					data.borderColor = a.getColor(attr, Color.BLACK);
				}
			}
		}
		finally
		{
			a.recycle();
		}
	}
	
	public static void onDrawHelper(Canvas canvas, TextView target, DrawCallback drawCallback)
	{
		if (target.isInEditMode())
			return;
		final ExtraFontData data = getFontData(target, false);
		if (data == null)
			return;
		
		if (data.borderWidth > 0) {
			final Paint paint = target.getPaint();
	
			// setup stroke
			final Style oldStyle = paint.getStyle();
			final ColorStateList oldTextColors = target.getTextColors();
			final float oldStrokeWidth = paint.getStrokeWidth();
			
			target.setTextColor(data.borderColor);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(data.borderWidth);
			callDrawCallback(drawCallback, canvas);
			
			target.setTextColor(oldTextColors);
			paint.setStyle(oldStyle);
			paint.setStrokeWidth(oldStrokeWidth);
		}
	}
	
	/**
	 * Calls the draw callback with the given canvas. Use this method instead of
	 * calling it yourself, as lint is fooled by the method name 'onDraw' and
	 * thinks we are intervening with the render cycle. With this method, we can
	 * isolate the suppress lint annotation to the only warning we want to
	 * suppress.
	 * 
	 * @param drawCallback
	 * @param canvas
	 */
	@SuppressLint("WrongCall")
	private static void callDrawCallback(DrawCallback drawCallback, Canvas canvas)
	{
		drawCallback.onDraw(canvas);
	}
	
	/**
	 * A data holder in which properties are stored that are not part of the
	 * default text view attributes, but which are applicable to all custom Font
	 * widgets. By storing this data holder in the corresponding view instance,
	 * we can apply the properties at any time with a shared static method.
	 * 
	 * @author Jelle Fresen <jelle@innovattic.com>
	 */
	public static class ExtraFontData
	{
		public String font;
		public int style;
		public int borderWidth;
		public int borderColor;

		public ExtraFontData(TextView target)
		{
			// By default, the font is not changed
			font = null;
			// By default, we apply a regular typeface
			style = Typeface.NORMAL;
			// By default, we don't add a border around the text
			borderWidth = 0;
			// By default, *if* there is a border, it will be black
			borderColor = Color.BLACK;
			// Store the data in the TextView
			target.setTag(R.id.flFontsExtraData, this);
		}
	}

	public static ExtraFontData getFontData(TextView target)
	{
		return getFontData(target, true);
	}

	public static ExtraFontData getFontData(TextView target, boolean createIfMissing)
	{
		ExtraFontData data = (ExtraFontData)target.getTag(R.id.flFontsExtraData);
		if (data == null && createIfMissing) {
			data = new ExtraFontData(target);
		}
		return data;
	}
	
	public interface DrawCallback
	{
		void onDraw(Canvas canvas);
	}

}
