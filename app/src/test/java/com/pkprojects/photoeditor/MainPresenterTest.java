package com.pkprojects.photoeditor;

import android.graphics.Bitmap;
import android.net.Uri;

import com.pkprojects.photoeditor.ui.MVPContract;
import com.pkprojects.photoeditor.ui.MainPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;


public class MainPresenterTest {

    private MainPresenter presenter;

    @Mock
    MVPContract.View view;

    @Mock
    MVPContract.Model model;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);// required for the "@Mock" annotations
        presenter = Mockito.spy(new MainPresenter(view, model));
    }

    @Test
    public void emptyBitmap_fail() {
        Bitmap b = Bitmap.createBitmap(null);
        assertEquals(presenter.imageSelected(b), false);
    }

    @Test
    public void emptyUri_fail() {
        Uri u = Uri.parse("");
        assertEquals(presenter.imageSelected(u), false);
    }

    @Test
    public void emptyImageFilter_fail() {
        assertEquals(presenter.filterSelected(0), false);
    }

    @Test
    public void emptyImageCreated_fail() {
        Bitmap b = Bitmap.createBitmap(null);
        assertEquals(presenter.imageCreated(b), false);
    }

    @Test
    public void filterSetting_valid() {
        presenter.filterSelected(1);
        assertEquals(presenter.getLastFilter(), 1);
    }
}