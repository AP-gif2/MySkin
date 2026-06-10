package org.hochschule_stralsund.myskin.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hochschule_stralsund.myskin.R;
import org.hochschule_stralsund.myskin.databinding.FragmentImageBinding;

public class ImageFragment extends Fragment {

    private FragmentImageBinding binding;
    private List<Bitmap> selectedBitmaps;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView currentSelectedImageView;
    private static final String IMAGE_DIR = "myskin_images";
    private static final int MAX_IMAGES = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedBitmaps = new ArrayList<>();
        setupPickImageLauncher();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentImageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        loadSavedImages();
    }

    private void setupClickListeners() {
        binding.btnSelectImages.setOnClickListener(v -> openGallery());
        binding.btnSaveImages.setOnClickListener(v -> saveImages());
        binding.btnDeleteAll.setOnClickListener(v -> deleteAllImages());

        // Image view click listeners
        setupImageViewClickListener(binding.imageView1, 0);
        setupImageViewClickListener(binding.imageView2, 1);
        setupImageViewClickListener(binding.imageView3, 2);
    }

    private void setupImageViewClickListener(ImageView imageView, int index) {
        imageView.setOnClickListener(v -> {
            currentSelectedImageView = imageView;
            showImageDialog(selectedBitmaps.get(index));
        });

        imageView.setOnLongClickListener(v -> {
            currentSelectedImageView = imageView;
            showReplaceDialog(index);
            return true;
        });
    }

    private void setupPickImageLauncher() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            loadImageFromUri(imageUri);
                        }
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void loadImageFromUri(Uri imageUri) {
        try {
            Context context = requireContext();
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (selectedBitmaps.size() < MAX_IMAGES) {
                selectedBitmaps.add(bitmap);
                updateImageViews();
            } else {
                Toast.makeText(context, "Maximum " + MAX_IMAGES + " images allowed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_loading_images), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageViews() {
        setImageViewBitmap(binding.imageView1, 0);
        setImageViewBitmap(binding.imageView2, 1);
        setImageViewBitmap(binding.imageView3, 2);
    }

    private void setImageViewBitmap(ImageView imageView, int index) {
        if (index < selectedBitmaps.size()) {
            imageView.setImageBitmap(selectedBitmaps.get(index));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView.setImageResource(android.R.color.darker_gray);
        }
    }

    private void saveImages() {
        try {
            File imageDir = new File(requireContext().getFilesDir(), IMAGE_DIR);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            for (int i = 0; i < selectedBitmaps.size(); i++) {
                File file = new File(imageDir, "image_" + i + ".png");
                FileOutputStream fos = new FileOutputStream(file);
                selectedBitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }

            Toast.makeText(requireContext(), getString(R.string.images_saved), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_saving_images), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedImages() {
        try {
            File imageDir = new File(requireContext().getFilesDir(), IMAGE_DIR);
            if (imageDir.exists()) {
                File[] files = imageDir.listFiles();
                if (files != null) {
                    selectedBitmaps.clear();
                    for (File file : files) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            selectedBitmaps.add(bitmap);
                        }
                    }
                    updateImageViews();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_loading_images), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllImages() {
        try {
            File imageDir = new File(requireContext().getFilesDir(), IMAGE_DIR);
            if (imageDir.exists()) {
                File[] files = imageDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
            selectedBitmaps.clear();
            updateImageViews();
            Toast.makeText(requireContext(), getString(R.string.images_deleted), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_saving_images), Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageDialog(Bitmap bitmap) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(new ImageView(requireContext()));
        ImageView imageView = (ImageView) dialog.findViewById(android.R.id.content);
        imageView.setImageBitmap(bitmap);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void showReplaceDialog(int imageIndex) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_image_options);
        
        dialog.findViewById(R.id.btn_replace).setOnClickListener(v -> {
            openGalleryForIndex(imageIndex);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            selectedBitmaps.remove(imageIndex);
            updateImageViews();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openGalleryForIndex(int index) {
        currentSelectedImageView = index == 0 ? binding.imageView1 : 
                                   index == 1 ? binding.imageView2 : binding.imageView3;
        openGallery();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
