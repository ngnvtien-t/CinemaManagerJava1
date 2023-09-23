package com.example.cinema.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cinema.MyApplication;
import com.example.cinema.R;
import com.example.cinema.adapter.FoodDrinkAdapter;
import com.example.cinema.adapter.RoomAdapter;
import com.example.cinema.adapter.SeatAdapter;
import com.example.cinema.adapter.SelectPaymentAdapter;
import com.example.cinema.adapter.TimeAdapter;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.constant.PayPalConfig;
import com.example.cinema.databinding.ActivityConfirmBookingBinding;
import com.example.cinema.listener.IOnSingleClickListener;
import com.example.cinema.model.BookingHistory;
import com.example.cinema.model.Food;
import com.example.cinema.model.Movie;
import com.example.cinema.model.PaymentMethod;
import com.example.cinema.model.Room;
import com.example.cinema.model.RoomFirebase;
import com.example.cinema.model.Seat;
import com.example.cinema.model.SeatLocal;
import com.example.cinema.model.SlotTime;
import com.example.cinema.model.TimeFirebase;
import com.example.cinema.prefs.DataStoreManager;
import com.example.cinema.util.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ConfirmBookingActivity extends AppCompatActivity {

    public static final int PAYPAL_REQUEST_CODE = 199;
    public static final String PAYPAL_PAYMENT_STATUS_APPROVED = "approved";
    //Paypal Configuration Object
    public static final PayPalConfiguration PAYPAL_CONFIG = new PayPalConfiguration()
            .environment(PayPalConfig.PAYPAL_ENVIRONMENT_DEV)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID_DEV)
            .acceptCreditCards(false);
    private Dialog mDialog;

    private ActivityConfirmBookingBinding mActivityConfirmBookingBinding;
    private Movie mMovie;

    private List<Room> mListRooms;
    private RoomAdapter mRoomAdapter;
    private String mTitleRoomSelected;

    private List<SlotTime> mListTimes;
    private TimeAdapter mTimeAdapter;
    private String mTitleTimeSelected;

    private List<Food> mListFood;
    private FoodDrinkAdapter mFoodDrinkAdapter;

    private List<SeatLocal> mListSeats;
    private SeatAdapter mSeatAdapter;

    private PaymentMethod mPaymentMethodSelected;
    private BookingHistory mBookingHistory;

    private List<Food> mListFoodNeedUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityConfirmBookingBinding = ActivityConfirmBookingBinding.inflate(getLayoutInflater());
        setContentView(mActivityConfirmBookingBinding.getRoot());

        getDataIntent();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        Movie movie = (Movie) bundle.get(ConstantKey.KEY_INTENT_MOVIE_OBJECT);
        getMovieInformation(movie.getId());
    }

    private void getMovieInformation(long movieId) {
        MyApplication.get(this).getMovieDatabaseReference().child(String.valueOf(movieId))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mMovie = snapshot.getValue(Movie.class);

                        displayDataMovie();
                        initListener();
                        initSpinnerCategory();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void displayDataMovie() {
        if (mMovie == null) {
            return;
        }
        mActivityConfirmBookingBinding.tvMovieName.setText(mMovie.getName());
        String strPrice = mMovie.getPrice() + ConstantKey.UNIT_CURRENCY_MOVIE;
        mActivityConfirmBookingBinding.tvMoviePrice.setText(strPrice);

        showListRooms();
        initListFoodAndDrink();
    }

    private void initListener() {
        mActivityConfirmBookingBinding.imgBack.setOnClickListener(view -> onBackPressed());
        mActivityConfirmBookingBinding.btnConfirm.setOnClickListener(view -> onClickBookingMovie());
    }

    private void showListRooms() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mActivityConfirmBookingBinding.rcvRoom.setLayoutManager(gridLayoutManager);

        mListRooms = getListRoomLocal();
        mRoomAdapter = new RoomAdapter(mListRooms, this::onClickSelectRoom);
        mActivityConfirmBookingBinding.rcvRoom.setAdapter(mRoomAdapter);
    }

    private List<Room> getListRoomLocal() {
        List<Room> list = new ArrayList<>();
        if (mMovie.getRooms() != null) {
            for (RoomFirebase roomFirebase : mMovie.getRooms()) {
                Room room = new Room(roomFirebase.getId(), roomFirebase.getTitle(), false);
                list.add(room);
            }
        }
        return list;
    }

    private String getTitleRoomSelected() {
        for (Room room : mListRooms) {
            if (room.isSelected()) {
                mTitleRoomSelected = room.getTitle();
                break;
            }
        }
        return mTitleRoomSelected;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onClickSelectRoom(Room room) {
        for (int i = 0; i < mListRooms.size(); i++) {
            mListRooms.get(i).setSelected(mListRooms.get(i).getId() == room.getId());
        }
        mRoomAdapter.notifyDataSetChanged();

        showListTimes(room.getId());
    }

    private RoomFirebase getRoomFirebaseFromId(int roomId) {
        RoomFirebase roomFirebase = new RoomFirebase();
        if (mMovie.getRooms() != null) {
            for (RoomFirebase roomFirebaseEntity : mMovie.getRooms()) {
                if (roomFirebaseEntity.getId() == roomId) {
                    roomFirebase = roomFirebaseEntity;
                    break;
                }
            }
        }
        return roomFirebase;
    }

    private void showListTimes(int roomId) {
        mActivityConfirmBookingBinding.layoutSelecteTime.setVisibility(View.VISIBLE);
        mActivityConfirmBookingBinding.layoutSelecteSeat.setVisibility(View.GONE);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mActivityConfirmBookingBinding.rcvTime.setLayoutManager(gridLayoutManager);

        mListTimes = getListTimeLocal(roomId);
        mTimeAdapter = new TimeAdapter(mListTimes, this::onClickSelectTime);
        mActivityConfirmBookingBinding.rcvTime.setAdapter(mTimeAdapter);
    }

    private List<SlotTime> getListTimeLocal(int roomId) {
        List<SlotTime> list = new ArrayList<>();
        RoomFirebase roomFirebase = getRoomFirebaseFromId(roomId);
        if (roomFirebase.getTimes() != null) {
            for (TimeFirebase timeFirebase : roomFirebase.getTimes()) {
                SlotTime slotTime = new SlotTime(timeFirebase.getId(), timeFirebase.getTitle(),
                        false, roomId);
                list.add(slotTime);
            }
        }
        return list;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onClickSelectTime(SlotTime time) {
        for (int i = 0; i < mListTimes.size(); i++) {
            mListTimes.get(i).setSelected(mListTimes.get(i).getId() == time.getId());
        }
        mTimeAdapter.notifyDataSetChanged();

        showListSeats(time);
    }

    private String getTitleTimeSelected() {
        for (SlotTime time : mListTimes) {
            if (time.isSelected()) {
                mTitleTimeSelected = time.getTitle();
                break;
            }
        }
        return mTitleTimeSelected;
    }

    private void showListSeats(SlotTime time) {
        mActivityConfirmBookingBinding.layoutSelecteSeat.setVisibility(View.VISIBLE);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 6);
        mActivityConfirmBookingBinding.rcvSeat.setLayoutManager(gridLayoutManager);

        mListSeats = getListSeatLocal(time);
        mSeatAdapter = new SeatAdapter(mListSeats, this::onClickItemSeat);
        mActivityConfirmBookingBinding.rcvSeat.setAdapter(mSeatAdapter);
    }

    private List<SeatLocal> getListSeatLocal(SlotTime time) {
        RoomFirebase roomFirebase = getRoomFirebaseFromId(time.getRoomId());
        TimeFirebase timeFirebase = getTimeFirebaseFromId(roomFirebase, time.getId());

        List<SeatLocal> list = new ArrayList<>();
        if (timeFirebase.getSeats() != null) {
            for (Seat seat : timeFirebase.getSeats()) {
                SeatLocal seatLocal = new SeatLocal(seat.getId(), seat.getTitle(),
                        seat.isSelected(), time.getRoomId(), time.getId());
                list.add(seatLocal);
            }
        }
        return list;
    }

    private TimeFirebase getTimeFirebaseFromId(RoomFirebase roomFirebase, int timeId) {
        TimeFirebase timeFirebase = new TimeFirebase();
        if (roomFirebase.getTimes() != null) {
            for (TimeFirebase timeFirebaseEntity : roomFirebase.getTimes()) {
                if (timeFirebaseEntity.getId() == timeId) {
                    timeFirebase = timeFirebaseEntity;
                    break;
                }
            }
        }
        return timeFirebase;
    }

    private Seat getSeatFirebaseFromId(int roomId, int timeId, int seatId) {
        RoomFirebase roomFirebase = getRoomFirebaseFromId(roomId);
        TimeFirebase timeFirebase = getTimeFirebaseFromId(roomFirebase, timeId);

        Seat seatResult = new Seat();
        if (timeFirebase.getSeats() != null) {
            for (Seat seat : timeFirebase.getSeats()) {
                if (seat.getId() == seatId) {
                    seatResult = seat;
                    break;
                }
            }
        }
        return seatResult;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onClickItemSeat(SeatLocal seat) {
        if (seat.isSelected()) {
            return;
        }
        seat.setChecked(!seat.isChecked());
        mSeatAdapter.notifyDataSetChanged();
    }

    private void initListFoodAndDrink() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mActivityConfirmBookingBinding.rcvFoodDrink.setLayoutManager(linearLayoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mActivityConfirmBookingBinding.rcvFoodDrink.addItemDecoration(decoration);

        getListFoodAndDrink();
    }

    public void getListFoodAndDrink() {
        MyApplication.get(this).getFoodDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListFood != null) {
                    mListFood.clear();
                } else {
                    mListFood = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Food food = dataSnapshot.getValue(Food.class);
                    if (food != null && food.getQuantity() > 0) {
                        mListFood.add(0, food);
                    }
                }
                mFoodDrinkAdapter = new FoodDrinkAdapter(mListFood, (food, count) -> selectedCountFoodAndDrink(food, count));
                mActivityConfirmBookingBinding.rcvFoodDrink.setAdapter(mFoodDrinkAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void selectedCountFoodAndDrink(Food food, int count) {
        if (mListFood == null || mListFood.isEmpty()) {
            return;
        }
        for (Food foodEntity : mListFood) {
            if (foodEntity.getId() == food.getId()) {
                foodEntity.setCount(count);
                break;
            }
        }
    }

    private void initSpinnerCategory() {
        List<PaymentMethod> list = new ArrayList<>();
        list.add(new PaymentMethod(ConstantKey.PAYMENT_CASH, ConstantKey.PAYMENT_CASH_TITLE));
        list.add(new PaymentMethod(ConstantKey.PAYMENT_PAYPAL, ConstantKey.PAYMENT_PAYPAL_TITLE));

        SelectPaymentAdapter selectPaymentAdapter = new SelectPaymentAdapter(this,
                R.layout.item_choose_option, list);
        mActivityConfirmBookingBinding.spnPayment.setAdapter(selectPaymentAdapter);
        mActivityConfirmBookingBinding.spnPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPaymentMethodSelected = selectPaymentAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void onClickBookingMovie() {
        if (mMovie == null) {
            return;
        }
        if (StringUtil.isEmpty(getTitleRoomSelected())) {
            Toast.makeText(this, getString(R.string.msg_select_room_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(getTitleTimeSelected())) {
            Toast.makeText(this, getString(R.string.msg_select_time_require), Toast.LENGTH_SHORT).show();
            return;
        }

        int countSeat = getListSeatChecked().size();
        if (countSeat <= 0) {
            Toast.makeText(this, getString(R.string.msg_count_seat), Toast.LENGTH_SHORT).show();
            return;
        }

        setListSeatUpdate();

        showDialogConfirmBooking();
    }

    private void setListSeatUpdate() {
        for (SeatLocal seatChecked : getListSeatChecked()) {
            getSeatFirebaseFromId(seatChecked.getRoomId(),
                    seatChecked.getTimeId(), seatChecked.getId()).setSelected(true);
        }
    }

    private void showDialogConfirmBooking() {
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.layout_dialog_confirm_booking);
        Window window = mDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setCancelable(false);

        // Get view
        final TextView tvNameMovie = mDialog.findViewById(R.id.tv_name_movie);
        final TextView tvDateMovie = mDialog.findViewById(R.id.tv_date_movie);
        final TextView tvRoomMovie = mDialog.findViewById(R.id.tv_room_movie);
        final TextView tvTimeMovie = mDialog.findViewById(R.id.tv_time_movie);
        final TextView tvCountBooking = mDialog.findViewById(R.id.tv_count_booking);
        final TextView tvCountSeat = mDialog.findViewById(R.id.tv_count_seat);
        final TextView tvFoodDrink = mDialog.findViewById(R.id.tv_food_drink);
        final TextView tvPaymentMethod = mDialog.findViewById(R.id.tv_payment_method);
        final TextView tvTotalAmount = mDialog.findViewById(R.id.tv_total_amount);

        final TextView tvDialogCancel = mDialog.findViewById(R.id.tv_dialog_cancel);
        final TextView tvDialogOk = mDialog.findViewById(R.id.tv_dialog_ok);

        // Set data
        int countView = getListSeatChecked().size();
        mListFoodNeedUpdate = new ArrayList<>(getListFoodSelected());

        tvNameMovie.setText(mMovie.getName());
        tvDateMovie.setText(mMovie.getDate());
        tvRoomMovie.setText(getTitleRoomSelected());
        tvTimeMovie.setText(getTitleTimeSelected());
        tvCountBooking.setText(String.valueOf(countView));
        tvCountSeat.setText(getStringSeatChecked());
        tvFoodDrink.setText(getStringFoodAndDrink());
        tvPaymentMethod.setText(mPaymentMethodSelected.getName());
        String strTotalAmount = getTotalAmount() + ConstantKey.UNIT_CURRENCY;
        tvTotalAmount.setText(strTotalAmount);

        // Set listener
        tvDialogCancel.setOnClickListener(new IOnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mDialog.dismiss();
            }
        });

        tvDialogOk.setOnClickListener(new IOnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                long id = System.currentTimeMillis();
                mBookingHistory = new BookingHistory(id, mMovie.getId(), mMovie.getName(),
                        mMovie.getDate(), getTitleRoomSelected(), getTitleTimeSelected(),
                        tvCountBooking.getText().toString(), getStringSeatChecked(),
                        getStringFoodAndDrink(), mPaymentMethodSelected.getName(),
                        getTotalAmount(), DataStoreManager.getUser().getEmail(), false);

                if (ConstantKey.PAYMENT_CASH == mPaymentMethodSelected.getType()) {
                    sendRequestOrder();
                } else {
                    getPaymentPaypal(getTotalAmount());
                }
            }
        });

        mDialog.show();
    }

    private void sendRequestOrder() {
        mMovie.setBooked(mMovie.getBooked() + Integer.parseInt(mBookingHistory.getCount()));
        MyApplication.get(ConfirmBookingActivity.this).getMovieDatabaseReference()
                .child(String.valueOf(mMovie.getId())).setValue(mMovie, (error, ref) ->
                MyApplication.get(ConfirmBookingActivity.this).getBookingDatabaseReference()
                        .child(String.valueOf(mBookingHistory.getId()))
                        .setValue(mBookingHistory, (error1, ref1) -> {

                            updateQuantityFoodDrink();

                            if (mDialog != null) mDialog.dismiss();
                            finish();

                            Toast.makeText(ConfirmBookingActivity.this,
                                    getString(R.string.msg_booking_movie_success), Toast.LENGTH_LONG).show();
                            GlobalFunction.hideSoftKeyboard(ConfirmBookingActivity.this);
                        }));
    }

    private void updateQuantityFoodDrink() {
        if (mListFoodNeedUpdate == null || mListFoodNeedUpdate.isEmpty()) {
            return;
        }
        for (Food food : mListFoodNeedUpdate) {
            changeQuantity(food.getId(), food.getCount());
        }
    }

    private void changeQuantity(long foodId, int quantity) {
        MyApplication.get(this).getQuantityDatabaseReference(foodId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer currentQuantity = snapshot.getValue(Integer.class);
                        if (currentQuantity != null) {
                            int totalQuantity = currentQuantity - quantity;
                            MyApplication.get(ConfirmBookingActivity.this).getQuantityDatabaseReference(foodId).removeEventListener(this);
                            MyApplication.get(ConfirmBookingActivity.this).getQuantityDatabaseReference(foodId).setValue(totalQuantity);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void getPaymentPaypal(int price) {
        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(price)),
                PayPalConfig.PAYPAL_CURRENCY, PayPalConfig.PAYPAl_CONTENT_TEXT,
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PAYPAL_CONFIG);

        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    private List<SeatLocal> getListSeatChecked() {
        List<SeatLocal> listSeatChecked = new ArrayList<>();
        if (mListSeats != null) {
            for (SeatLocal seat : mListSeats) {
                if (seat.isChecked()) {
                    listSeatChecked.add(seat);
                }
            }
        }
        return listSeatChecked;
    }

    private List<Food> getListFoodSelected() {
        List<Food> listFoodSelected = new ArrayList<>();
        if (mListFood != null) {
            for (Food food : mListFood) {
                if (food.getCount() > 0) {
                    listFoodSelected.add(food);
                }
            }
        }
        return listFoodSelected;
    }

    private String getStringFoodAndDrink() {
        String result = "";
        List<Food> listFoodSelected = getListFoodSelected();
        if (listFoodSelected.isEmpty()) {
            return "Không";
        }
        for (Food food : listFoodSelected) {
            if (StringUtil.isEmpty(result)) {
                result = food.getName() + " (" + food.getPrice()
                        + ConstantKey.UNIT_CURRENCY + ")"
                        + " - Số lượng: " + food.getCount();
            } else {
                result = result + "\n"
                        + food.getName() + " (" + food.getPrice()
                        + ConstantKey.UNIT_CURRENCY + ")"
                        + " - Số lượng: " + food.getCount();
            }
        }

        return result;
    }

    private String getStringSeatChecked() {
        String result = "";
        List<SeatLocal> listSeatChecked = getListSeatChecked();
        for (SeatLocal seatLocal : listSeatChecked) {
            if (StringUtil.isEmpty(result)) {
                result = seatLocal.getTitle();
            } else {
                result = result + ", " + seatLocal.getTitle();
            }
        }

        return result;
    }

    private int getTotalAmount() {
        if (mMovie == null) {
            return 0;
        }
        int countBooking = 0;
        try {
            countBooking = getListSeatChecked().size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int priceMovie = countBooking * mMovie.getPrice();

        int priceFoodDrink = 0;
        List<Food> listFoodSelected = getListFoodSelected();
        if (!listFoodSelected.isEmpty()) {
            for (Food food : listFoodSelected) {
                priceFoodDrink = priceFoodDrink + food.getPrice() * food.getCount();
            }
        }

        return priceMovie + priceFoodDrink;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            boolean isPaymentSuccess = false;

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.e("Payment Result", paymentDetails);

                        JSONObject jsonDetails = new JSONObject(paymentDetails);
                        JSONObject jsonResponse = jsonDetails.getJSONObject("response");
                        String strState = jsonResponse.getString("state");
                        Log.e("Payment State", strState);
                        if (PAYPAL_PAYMENT_STATUS_APPROVED.equals(strState)) {
                            isPaymentSuccess = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_payment_error), Toast.LENGTH_SHORT).show();
            }

            // Send result payment
            if (isPaymentSuccess) sendRequestOrder();
        }
    }
}