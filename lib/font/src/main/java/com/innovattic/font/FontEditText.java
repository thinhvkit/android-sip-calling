package com.innovattic.font;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.EditText;

import com.innovattic.font.TypefaceManager.DrawCallback;

public class FontEditText extends EditText
{

	private static final int NO_LINE_LIMIT = -1;
	private final RectF _availableSpaceRect = new RectF();
	private final SparseIntArray _textCachedSizes = new SparseIntArray();
	private final SizeTester _sizeTester;
	private float _maxTextSize;
	private float _spacingMult = 1.0f;
	private float _spacingAdd = 0.0f;
	private float _minTextSize;
	private int _widthLimit;
	private int _maxLines;
	private boolean _enableSizeCache = true;
	private boolean _initiallized = false;
	private TextPaint paint;
	private Paint underlinePaint;
	private int underlineHeight = 2;
	private int underlineColor = 0xFFFFFF;


	private interface SizeTester {
		/**
		 * AutoResizeEditText
		 *
		 * @param suggestedSize
		 *            Size of text to be tested
		 * @param availableSpace
		 *            available space in which text must fit
		 * @return an integer < 0 if after applying {@code suggestedSize} to
		 *         text, it takes less space than {@code availableSpace}, > 0
		 *         otherwise
		 */
		int onTestSize(int suggestedSize, RectF availableSpace);
	}

	public FontEditText(Context context)
	{
		this(context, null, 0);
	}

	public FontEditText(Context context, AttributeSet attrs)
	{
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public FontEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		if (!isInEditMode())
			TypefaceManager.applyFont(this, attrs, defStyle);
		// using the minimal recommended font size
		_minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				12, getResources().getDisplayMetrics());
		_maxTextSize = getTextSize();
		if (_maxLines == 0)
			// no value was assigned during construction
			_maxLines = NO_LINE_LIMIT;
		// prepare size tester:
		_sizeTester = new SizeTester() {
			final RectF textRect = new RectF();

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public int onTestSize(final int suggestedSize,
								  final RectF availableSPace) {
				paint.setTextSize(suggestedSize);
				final String text = getText().toString();
				final boolean singleline = getMaxLines() == 1;
				if (singleline) {
					textRect.bottom = paint.getFontSpacing();
					textRect.right = paint.measureText(text);
				} else {
					final StaticLayout layout = new StaticLayout(text, paint,
							_widthLimit, Layout.Alignment.ALIGN_NORMAL, _spacingMult,
							_spacingAdd, true);
					// return early if we have more lines
					Log.d("NLN", "Current Lines = " + Integer.toString(layout.getLineCount()));
					Log.d("NLN", "Max Lines = "+Integer.toString(getMaxLines()));
					if (getMaxLines() != NO_LINE_LIMIT
							&& layout.getLineCount() > getMaxLines())
						return 1;
					textRect.bottom = layout.getHeight();
					int maxWidth = -1;
					for (int i = 0; i < layout.getLineCount(); i++)
						if (maxWidth < layout.getLineWidth(i))
							maxWidth = (int) layout.getLineWidth(i);
					textRect.right = maxWidth;
				}
				textRect.offsetTo(0, 0);
				if (availableSPace.contains(textRect))
					// may be too small, don't worry we will find the best match
					return -1;
				// else, too big
				return 1;
			}
		};
		_initiallized = true;


		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontEditText);
		underlineHeight = a.getInt(R.styleable.FontEditText_flunderlineHeight, underlineHeight);
		underlineColor = a.getResourceId(R.styleable.FontEditText_flunderlineColor, underlineColor);

		underlinePaint = new Paint();
		underlinePaint.setStyle(Paint.Style.FILL);
//        underlinePaint.setColor(Color.RED); //SET YOUR OWN COLOR HERE
//        mPaint.setStrokeWidth(underlineHeight*2);
		underlinePaint.setAntiAlias(true);


	}

	private final DrawCallback drawCallback = new DrawCallback() {
		@SuppressLint("WrongCall")
		@Override public void onDraw(Canvas canvas) {
			FontEditText.super.onDraw(canvas);
		}
	};

	@Override
	protected void onDraw(Canvas canvas)
	{
		try {
			XmlResourceParser parser = getResources().getXml(underlineColor);
			ColorStateList colors = ColorStateList.createFromXml(getResources(), parser);
			int[] focusedState = {android.R.attr.state_focused};
			if (isFocused())
				underlinePaint.setColor(colors.getColorForState(focusedState, colors.getDefaultColor()));
			else
				underlinePaint.setColor(colors.getDefaultColor());
		} catch (Exception e) {
			// handle exceptions
		}
		float size = underlinePaint.measureText(getText().toString());

//        canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);
		//underline
		canvas.drawRect(0, getHeight() - underlineHeight * getResources().getDisplayMetrics().density, getWidth() + size * getTextSize(), getHeight(), underlinePaint);
		//leftline
//        canvas.drawRect(0, getHeight() - underlineHeight - 5, underlineHeight, getHeight(), underlinePaint);
//        //rightline
//        canvas.drawRect(getWidth() - underlineHeight, getHeight() - underlineHeight - 5, getWidth(), getHeight(), underlinePaint);

//        Paint paint = mPaint;

//        for (int i = 0; i < count; i++) {


//        canvas.drawLine(left, bottom, right, bottom, paint);
//        canvas.drawLine(left, bottom + underlineHeight, left, bottom, paint);
//        canvas.drawLine(right, bottom + underlineHeight, right, bottom, paint);
//        }

		TypefaceManager.onDrawHelper(canvas, this, drawCallback);
		super.onDraw(canvas);
//        this.setBackgroundColor(0xFFFFFF);
//        Paint leftBar = new Paint();
//        leftBar.setColor(0x000000);
//        leftBar.setAntiAlias(true);
//        leftBar.setStyle(Paint.Style.FILL);
//
//        canvas.drawRect(getWidth() / 2, getHeight() - 30, getWidth() / 2, getHeight(), leftBar);
	}


	@Override
	public void setTypeface(final Typeface tf) {
		if (paint == null)
			paint = new TextPaint(getPaint());
		paint.setTypeface(tf);
		super.setTypeface(tf);
	}

	@Override
	public void setTextSize(final float size) {
		_maxTextSize = size;
		_textCachedSizes.clear();
		adjustTextSize();
	}

	@Override
	public void setMaxLines(final int maxlines) {
		super.setMaxLines(maxlines);
		_maxLines = maxlines;
		reAdjust();
	}

	@Override
	public int getMaxLines() {
		return _maxLines;
	}

	@Override
	public void setSingleLine() {
		super.setSingleLine();
		_maxLines = 1;
		reAdjust();
	}

	@Override
	public void setSingleLine(final boolean singleLine) {
		super.setSingleLine(singleLine);
		if (singleLine)
			_maxLines = 1;
		else
			_maxLines = NO_LINE_LIMIT;
		reAdjust();
	}

	@Override
	public void setLines(final int lines) {
		super.setLines(lines);
		_maxLines = lines;
		reAdjust();
	}

	@Override
	public void setTextSize(final int unit, final float size) {
		final Context c = getContext();
		Resources r;
		if (c == null)
			r = Resources.getSystem();
		else
			r = c.getResources();
		_maxTextSize = TypedValue.applyDimension(unit, size,
				r.getDisplayMetrics());
		_textCachedSizes.clear();
		adjustTextSize();
	}

	@Override
	public void setLineSpacing(final float add, final float mult) {
		super.setLineSpacing(add, mult);
		_spacingMult = mult;
		_spacingAdd = add;
	}

	/**
	 * Set the lower text size limit and invalidate the view
	 *
	 * @param

	 */
	public void setMinTextSize(final float minTextSize) {
		_minTextSize = minTextSize;
		reAdjust();
	}

	public void setMinTextSizeDependOnMaxSize(final float minTextSize) {
		_minTextSize = _maxTextSize - minTextSize;
		reAdjust();
	}

	private void reAdjust() {
		adjustTextSize();
	}

	private void adjustTextSize() {
		if (!_initiallized)
			return;
		final int startSize = (int) _minTextSize;
		final int heightLimit = getMeasuredHeight()
				- getCompoundPaddingBottom() - getCompoundPaddingTop();
		_widthLimit = getMeasuredWidth() - getCompoundPaddingLeft()
				- getCompoundPaddingRight();
		if (_widthLimit <= 0)
			return;
		_availableSpaceRect.right = _widthLimit;
		_availableSpaceRect.bottom = heightLimit;
		super.setTextSize(
				TypedValue.COMPLEX_UNIT_PX,
				efficientTextSizeSearch(startSize, (int) _maxTextSize,
						_sizeTester, _availableSpaceRect));
	}

	/**
	 * Enables or disables size caching, enabling it will improve performance
	 * where you are animating a value inside TextView. This stores the font
	 * size against getText().length() Be careful though while enabling it as 0
	 * takes more space than 1 on some fonts and so on.
	 *
	 * @param enable
	 *            enable font size caching
	 */
	public void setEnableSizeCache(final boolean enable) {
		_enableSizeCache = enable;
		_textCachedSizes.clear();
		adjustTextSize();
	}

	private int efficientTextSizeSearch(final int start, final int end,
										final SizeTester sizeTester, final RectF availableSpace) {
		if (!_enableSizeCache)
			return binarySearch(start, end, sizeTester, availableSpace);
		final String text = getText().toString();
		final int key = text == null ? 0 : text.length();
		int size = _textCachedSizes.get(key);
		if (size != 0)
			return size;
		size = binarySearch(start, end, sizeTester, availableSpace);
		_textCachedSizes.put(key, size);
		return size;
	}

	private int binarySearch(final int start, final int end,
							 final SizeTester sizeTester, final RectF availableSpace) {
		int lastBest = start;
		int lo = start;
		int hi = end - 1;
		int mid = 0;
		while (lo <= hi) {
			mid = lo + hi >>> 1;
			final int midValCmp = sizeTester.onTestSize(mid, availableSpace);
			if (midValCmp < 0) {
				lastBest = lo;
				lo = mid + 1;
			} else if (midValCmp > 0) {
				hi = mid - 1;
				lastBest = hi;
			} else
				return mid;
		}
		// make sure to return last best
		// this is what should always be returned
		return lastBest;
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start,
								 final int before, final int after) {
		super.onTextChanged(text, start, before, after);
		reAdjust();
	}

	@Override
	protected void onSizeChanged(final int width, final int height,
								 final int oldwidth, final int oldheight) {
		_textCachedSizes.clear();
		super.onSizeChanged(width, height, oldwidth, oldheight);
		if (width != oldwidth || height != oldheight)
			reAdjust();
	}
}
