package com.yydaniel.bestdoricarddownloader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.yydaniel.bestdoricarddownloader.CardBundle;

public class AddCardManuallyDialog extends DialogFragment {
    private OnDialogInteractionListener listener;

    // 定义回调接口
    public interface OnDialogInteractionListener {
        void onButtonActionRequested(CardBundle bundle);
    }

    // 设置监听器（由 Activity 实现）
    public void setListener(OnDialogInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_card_manually, null);
        builder.setView(view)
                .setTitle("添加卡面")
                .setPositiveButton(R.string.button_confirm_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cardId = 0;
                        String region = "";
                        boolean trainingState = false;
                        EditText et = view.findViewById(R.id.editTextNumber);
                        cardId = Integer.parseInt(String.valueOf(et.getText()));
                        RadioGroup rg_region = view.findViewById(R.id.rg_region);
                        RadioGroup rg_trainingState = view.findViewById(R.id.rg_training_state);

                        for(int i = 0; i < 5; i++) {
                            RadioButton rb = (RadioButton)rg_region.getChildAt(i);
                            if(rb.isChecked()) {
                                switch (i) {
                                    case 0: region = "jp"; break;
                                    case 1: region = "en"; break;
                                    case 2: region = "cn"; break;
                                    case 3: region = "tw"; break;
                                    case 4: region = "kr"; break;
                                    default: region = "cn";
                                }
                            }
                        }

                        for(int i = 0; i < 2; i++) {
                            RadioButton rb = (RadioButton)rg_trainingState.getChildAt(i);
                            if(rb.isChecked()) {
                                switch (i) {
                                    case 0: trainingState = false; break;
                                    case 1: trainingState = true; break;
                                    default: trainingState = false;
                                }
                            }
                        }

                        CardBundle bundle = new CardBundle(cardId, region, trainingState);
                        if(listener != null) {
                            listener.onButtonActionRequested(bundle);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
