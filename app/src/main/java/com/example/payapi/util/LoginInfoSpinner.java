package com.example.payapi.util;

import com.example.payapi.R;

import android.view.View;
import android.view.View.OnClickListener;

public class LoginInfoSpinner  implements OnClickListener 
{

	@Override
	public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.button1:
        	OnClickInfo();
            break;
        }
	}
	private void OnClickInfo()
	{
		
	}
	
}
