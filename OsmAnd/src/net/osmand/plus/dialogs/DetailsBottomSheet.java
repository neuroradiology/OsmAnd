package net.osmand.plus.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.osmand.plus.ContextMenuAdapter;
import net.osmand.plus.ContextMenuItem;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemTwoChoicesButton;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemTwoChoicesButton.OnBottomBtnClickListener;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithCompoundButton;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.DividerItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.ShortDescriptionItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.settings.backend.OsmandSettings.CommonPreference;
import net.osmand.plus.settings.bottomsheets.BasePreferenceBottomSheet;
import net.osmand.render.RenderingRuleProperty;
import net.osmand.render.RenderingRuleStorageProperties;

import java.util.ArrayList;
import java.util.List;

import static net.osmand.plus.transport.TransportLinesMenu.RENDERING_CATEGORY_TRANSPORT;
import static net.osmand.render.RenderingRuleStorageProperties.UI_CATEGORY_DETAILS;

public class DetailsBottomSheet extends BasePreferenceBottomSheet {

	public static final String TAG = DetailsBottomSheet.class.getName();
	public static final String STREET_LIGHTING = "streetLighting";
	public static final String STREET_LIGHTING_NIGHT = "streetLightingNight";
	public static final String MORE_DETAILED = "moreDetailed";
	public static final String SHOW_SURFACE_GRADE = "showSurfaceGrade";
	public static final String COLORED_BUILDINGS = "coloredBuildings";
	private OsmandApplication app;
	private List<RenderingRuleProperty> properties;
	private List<CommonPreference<Boolean>> preferences;
	private ArrayAdapter<?> arrayAdapter;
	private ContextMenuAdapter adapter;
	private int position;

	public static void showInstance(@NonNull FragmentManager fm,
									List<RenderingRuleProperty> properties,
									List<CommonPreference<Boolean>> preferences,
									ArrayAdapter<?> arrayAdapter,
									ContextMenuAdapter adapter,
									int position) {
		if (!fm.isStateSaved()) {
			DetailsBottomSheet bottomSheet = new DetailsBottomSheet();
			bottomSheet.setProperties(properties);
			bottomSheet.setPreferences(preferences);
			bottomSheet.setAdapter(adapter);
			bottomSheet.setPosition(position);
			bottomSheet.setArrayAdapter(arrayAdapter);
			bottomSheet.show(fm, TAG);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = requiredMyApplication();
		if (properties == null || preferences == null) {
			properties = new ArrayList<>();
			preferences = new ArrayList<>();
			List<RenderingRuleProperty> customRules = ConfigureMapMenu.getCustomRules(app,
					RenderingRuleStorageProperties.UI_CATEGORY_HIDDEN, RENDERING_CATEGORY_TRANSPORT);
			for (RenderingRuleProperty pr : customRules) {
				if (UI_CATEGORY_DETAILS.equals(pr.getCategory()) && pr.isBoolean()) {
					properties.add(pr);
					final CommonPreference<Boolean> pref = app.getSettings()
							.getCustomRenderBooleanProperty(pr.getAttrName());
					preferences.add(pref);
				}
			}
		}
	}

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		int selectedProfileColorRes = app.getSettings().APPLICATION_MODE.get().getIconColorInfo().getColor(nightMode);
		TitleItem titleItem = new TitleItem(getString(R.string.rendering_category_details));
		items.add(titleItem);
		ShortDescriptionItem descriptionItem = new ShortDescriptionItem(getString(R.string.details_dialog_decr));
		items.add(descriptionItem);
		if (preferences != null && properties != null) {
			RenderingRuleProperty streetLightNightProp = getStreetLightNightProp();
			for (int i = 0; i < properties.size(); i++) {
				RenderingRuleProperty property = properties.get(i);
				final CommonPreference<Boolean> pref = preferences.get(i);
				if (STREET_LIGHTING.equals(property.getAttrName()) && streetLightNightProp != null) {
					final CommonPreference<Boolean> streetLightsNightPref = preferences.get(properties.indexOf(streetLightNightProp));
					final BottomSheetItemTwoChoicesButton[] item = new BottomSheetItemTwoChoicesButton[1];
					item[0] = (BottomSheetItemTwoChoicesButton) new BottomSheetItemTwoChoicesButton.Builder()
							.setLeftBtnSelected(!streetLightsNightPref.get())
							.setLeftBtnTitleRes(R.string.shared_string_all_time)
							.setRightBtnTitleRes(R.string.shared_string_night_map)
							.setOnBottomBtnClickListener(new OnBottomBtnClickListener() {
								@Override
								public void onBottomBtnClick(boolean onLeftClick) {
									streetLightsNightPref.set(!onLeftClick);
								}
							})
							.setCompoundButtonColorId(selectedProfileColorRes)
							.setChecked(pref.get())
							.setTitle(property.getName())
							.setIconHidden(true)
							.setLayoutId(R.layout.bottom_sheet_item_two_choices)
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									boolean checked = !pref.get();
									pref.set(checked);
									streetLightsNightPref.set(false);
									item[0].setChecked(checked);
									item[0].setIsLeftBtnSelected(true);
								}
							})
							.create();
					items.add(item[0]);
				} else if (!STREET_LIGHTING_NIGHT.equals(property.getAttrName())) {
					final BottomSheetItemWithCompoundButton[] item = new BottomSheetItemWithCompoundButton[1];
					item[0] = (BottomSheetItemWithCompoundButton) new BottomSheetItemWithCompoundButton.Builder()
							.setCompoundButtonColorId(selectedProfileColorRes)
							.setChecked(pref.get())
							.setTitle(property.getName())
							.setIconHidden(true)
							.setLayoutId(R.layout.bottom_sheet_item_with_switch)
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									boolean checked = !pref.get();
									pref.set(checked);
									item[0].setChecked(checked);
								}
							})
							.create();
					items.add(item[0]);
				}
				String attrName = property.getAttrName();
				if (MORE_DETAILED.equals(attrName) || SHOW_SURFACE_GRADE.equals(attrName)
						|| COLORED_BUILDINGS.equals(attrName) || STREET_LIGHTING.equals(attrName)) {
					items.add(new DividerItem(app));
				}
			}
		}
	}

	@Nullable
	private RenderingRuleProperty getStreetLightNightProp() {
		if (properties != null) {
			for (RenderingRuleProperty property : properties) {
				if (STREET_LIGHTING_NIGHT.equals(property.getAttrName())) {
					return property;
				}
			}
		}
		return null;
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_close;
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		boolean checked = false;
		int selected = 0;
		for (int i = 0; i < preferences.size(); i++) {
			boolean active = preferences.get(i).get();
			checked |= active;
			if (active) {
				selected++;
			}
		}
		if (adapter != null) {
			adapter.getItem(position).setSelected(checked);
			adapter.getItem(position).setColorRes(checked ? R.color.osmand_orange : ContextMenuItem.INVALID_ID);
			adapter.getItem(position).setDescription(getString(
					R.string.ltr_or_rtl_combine_via_slash,
					String.valueOf(selected),
					String.valueOf(preferences.size())));
		}
		if (arrayAdapter != null) {
			arrayAdapter.notifyDataSetInvalidated();
		}
		Activity activity = getActivity();
		if (activity instanceof MapActivity) {
			MapActivity a = (MapActivity) activity;
			ConfigureMapMenu.refreshMapComplete(a);
			a.getMapLayers().updateLayers(a.getMapView());
		}
		super.onDismiss(dialog);
	}

	public void setProperties(List<RenderingRuleProperty> properties) {
		this.properties = properties;
	}

	public void setPreferences(List<CommonPreference<Boolean>> preferences) {
		this.preferences = preferences;
	}

	public void setAdapter(ContextMenuAdapter adapter) {
		this.adapter = adapter;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setArrayAdapter(ArrayAdapter<?> arrayAdapter) {
		this.arrayAdapter = arrayAdapter;
	}
}