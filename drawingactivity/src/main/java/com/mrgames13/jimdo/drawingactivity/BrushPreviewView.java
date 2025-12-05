package com.mrgames13.jimdo.drawingactivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.raed.rasmview.RasmContext;
import com.raed.rasmview.brushtool.model.BrushConfig;

public class BrushPreviewView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    @Nullable
    private RasmContext rasmContext;

    public BrushPreviewView(Context context) {
        super(context);
    }

    public BrushPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrushPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRasmContext(@Nullable RasmContext context) {
        this.rasmContext = context;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rasmContext == null) return;

        BrushConfig config = rasmContext.getBrushConfig();
        int color = rasmContext.getBrushColor();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        float size = config.getSize(); // 0..1
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f * size;

        canvas.drawCircle(cx, cy, radius, paint);
    }
}
