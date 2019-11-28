package za.co.xisystems.itis_rrm.ui.mainview.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import org.greenrobot.eventbus.EventBus;
import za.co.xisystems.itis_rrm.R;


public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private TextView serviceVersionTextView;
    private boolean errorOccurredDuringRegistration;

    public static final String HOME = "general_switch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        EventBus.getDefault().register(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        serviceVersionTextView = findViewById(R.id.serviceVersionTextView);

//        createDataSources();
//        openDataSources();
//        healthCheckInteractor = new HealthCheckInteractor();
        // Initialize the controllers necessary for communication with the datasources
//        initializeControllers();


//        Button reFreshButton = (Button) findViewById(R.id.refresh_lookups_button);
//        reFreshButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                refreshWorkFlowItemsViewModel.refreshWorkFlowItems();
//                Toast.makeText(getApplicationContext(), "Refresh Workflow is on Auto Sync", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Button reFreshbtnn = (Button) findViewById(R.id.refresh_lookups);
//        reFreshbtnn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ServiceUtil.isNetworkConnected(getApplicationContext())) {
//                    // Checks whether there are unsubmitted jobs.  If there are, then do not refresh any contract info or lookup tables
//                    if (!jobDataController.unsubmittedJobsExist()) {
//                        deleteSQLiteLookupTables();
//
//                        refreshLookupDataViewModel.advancedSearchSections(registrationInfoDataSource.getUserId());
//                    } else {
//                        Toast.makeText(getApplicationContext(), "You have unsubmitted jobs. Please submit or delete the jobs and then try again.",
//                                Toast.LENGTH_LONG).show();
//                    }
//
//                    if (PermissionController.checkPermissionsEnabled(getApplicationContext())) {
//                        //refreshToDoListItems();
//                    } else {
//                        //PermissionController.startPermissionRequests(this, getApplicationContext());
//                    }
//                } else
//                    Toast.makeText(getApplicationContext(), getString(R.string.no_connection_detected), Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "Clean and Refresh from your settings", Toast.LENGTH_SHORT).show();
//            }
//        });





        Button resetPinButton = (Button) findViewById(R.id.reset_pin_button);
        resetPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the reset pin screen
//                Intent resetPinIntent = new Intent(getApplicationContext(), ResetPinActivity.class);
//                startActivity(resetPinIntent);
                finish();
            }
        });

        Button resetAppButton = (Button) findViewById(R.id.button_reset_app);
        resetAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
                builder.setTitle(R.string.confirm);
                builder.setMessage(R.string.all_data_will_be_deleted_are_you_sure);

                // Yes button
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear out all the photos on the device
//                        deletePhotosInDirectory();

                        // Clear all data from all of the ITIS tables
//                        BaseDataSource baseDataSource = new BaseDataSource(getApplicationContext());
//                        SQLiteDatabase sqLiteDatabase = baseDataSource.getDatabase();
//                        if (sqLiteDatabase != null) {
//                            MySQLiteHelper.clearAllItisTables(sqLiteDatabase);
//                        }

                        // Take user back to the Registration screen
//                        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);/
//                        startActivity(registerIntent);
                        finish();
                    }
                });

                // No button
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close dialog
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        TextView serverTextView = findViewById(R.id.serverUrlTextView);
//        serverTextView.setText("\nServer: " + ServiceUriUtil.getWebServiceRootUri());

        // Run device cleanup
//        cleanupDevice();
//        initRegisterInfo();
    }


//    private void initRegisterInfo() {
//        if (jobDataController != null) {
//            RegistrationInfo registerinfo = jobDataController.getRegistrationInfo();
//            if (registerinfo != null) {
//                String username = registerinfo.getUsername();
//                TextView welcome = (TextView) findViewById(R.id.username1);
//                welcome.setText("Welcome " + username + ".");
//            }
//        }
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void updateMenuDrawer(UpdateMenuDrawerEvent event) {
//        initRegisterInfo();
//
//    }







    // WebAPI 2.0 Service Call
//    private void refreshContractInfo() {
//        ContractsViewModel contractsViewModel = new ContractsViewModel();
//        String[] messages = {getString(R.string.refreshing_contract_related_info) + getString(R.string.new_line) + getString(R.string.please_wait)};
//        int id = registrationInfoDataSource.getUserId();
//        contractsViewModel.refreshContractInfo(id, new BaseCallBackView<ContractRefreshResponse>(this, messages) {
//            @Override
//            public void processData(ContractRefreshResponse response) {
//                if (response.getOfflinedata() != null) {
//                    ArrayList<Contracts> offlinedata = response.getOfflinedata();
//                    registerDataController.insertContractRelatedInfo(offlinedata);
//                    // Get the effective roles
//                    refreshEffectiveRoles();
//                } else {
//                    toast(R.string.refreshing_contract_info_failed);
//                }
//            }
//        });
//    }

    // WebAPI 2.0 Service Call
//    private void refreshEffectiveRoles() {
//        String[] messages = {getString(R.string.refreshing_effective_roles) + getString(R.string.new_line) +
//                getString(R.string.please_wait)};
//        EffectiveRolesRefreshViewModel effectiveRolesRefreshViewModel = new EffectiveRolesRefreshViewModel();
//        final int id = registrationInfoDataSource.getUserId();
//        effectiveRolesRefreshViewModel.refreshEffectiveRoles(id, new BaseCallBackView<EffectiveRolesRefreshResponse>(this, messages) {
//            @Override
//            public void processData(EffectiveRolesRefreshResponse response) {
//                if (response.getEffectiveRoles() != null) {
//                    ArrayList<Role> effectiveRoles = response.getEffectiveRoles();
//                    registerDataController.insertEffectiveRoles(effectiveRoles);
//                    // Get the user roles
//                    refreshUserRoles();
//                } else {
//                    toast(R.string.refreshing_effective_roles_failed);
//                }
//            }
//        });
//    }

    // WebAPI 2.0 Service Call
//    private void refreshUserRoles() {
//        UserRolesRefreshViewModel userRolesRefreshViewModel = new UserRolesRefreshViewModel();
//        String messages[] = {getString(R.string.refreshing_user_roles) + getString(R.string.new_line) + getString(R.string.please_wait)};
//        int id = registrationInfoDataSource.getUserId();
//        userRolesRefreshViewModel.refreshUserRoles(id, new BaseCallBackView<UserRolesRefreshResponse>(this, messages) {
//
//            @Override
//            public void processData(UserRolesRefreshResponse response) {
//                processUserRolesResponse(response);
//            }
//        });
//    }

//    private void processUserRolesResponse(@Nullable UserRolesRefreshResponse response) {
//        if (response != null) {
//            if (response.isSuccess()) {
//                Log.i(TAG, getString(R.string.response_was_a_success));
//                if (response.getUserRoles() != null) {
//
//                    ArrayList<Role> userRoles = response.getUserRoles();
//                    registerDataController.insertUserRoles(userRoles);
//                    // Get the workflow info
//                    refreshWorkflows();
//                } else {
//                    toast(R.string.refreshing_user_roles_failed);
//                }
//            } else {
//                toast(response.getErrorMessage());
//            }
//        } else {
//            toast(R.string.refreshing_user_roles_failed);
//        }
//    }

    // WebAPI 2.0 Service Call
//    private void refreshWorkflows() {
//        String[] messages = {getString(R.string.refreshing_workflows) + getString(R.string.new_line) + getString(R.string.please_wait)};
//        WorkflowsRefreshViewModel workflowsRefreshViewModel = new WorkflowsRefreshViewModel();
//        int id = registrationInfoDataSource.getUserId();
//        workflowsRefreshViewModel.refreshWorkflows(id, new BaseCallBackView<WorkflowsRefreshResponse>(this, messages) {
//            @Override
//            public void processData(WorkflowsRefreshResponse response) {
//                MobileWorkFlows workflows = response.getWorkflows();
//                registerDataController.insertWorkFlows(workflows);
//                // refresh all the lookups
//                refreshLookups();
//            }
//        });
//    }

//    // WebAPI 2.0 Service Call
//    private void refreshLookups() {
//        String[] messages = {getString(R.string.refreshing_lookups) + getString(R.string.new_line) + getString(R.string.please_wait)};
//        int id = registrationInfoDataSource.getUserId();
//        MobileLookupsRefreshViewModel mobileLookupsRefreshViewModel = new MobileLookupsRefreshViewModel();
//        mobileLookupsRefreshViewModel.refreshMobileLookups(id, new BaseCallBackView<MobileLookupsRefreshResponse>(this, messages) {
//            @Override
//            public void processData(MobileLookupsRefreshResponse response) {
//                ArrayList<MobileLookup2> mobileLookups = response.getMobileLookups();
//                registerDataController.insertMobileLookups2(mobileLookups);
//                //  Lest refresh WorkFlow items
//                refreshWorkFlowItemsViewModel.refreshWorkFlowItems();
//            }
//        });
//    }

//    private void cleanupDevice() {
//
//        try {
//            // Get a list of JobIds from jobs of which the ActId is 4 (complete) and 5 (failed)
//            ArrayList<String> jobIds = jobDataController.getJobIds(ActivityIdConstants.JOB_COMPLETED);
//            ArrayList<String> failedCancelledJobIds = jobDataController.getJobIds(ActivityIdConstants.JOB_FAILED);
//            jobIds.addAll(failedCancelledJobIds);
//
//            // Get a list of job_items_measure_ids from job_items_measure where job_id in jobIds
//            ArrayList<String> jobItemMeasureIds = jobDataController.getJobItemMeasureIds(jobIds);
//
//            // Get a list of photo paths and filenames from job_item_measure_photo where job_items_measure_id in jobItemMeasureIds
//            ArrayList<String> jobItemMeasurePhotoIds = jobDataController.getJobItemMeasurePhotoIds(jobItemMeasureIds);
//            ArrayList<String> jobItemMeasurePhotos = jobDataController.getJobItemMeasurePhotos(jobItemMeasureIds);
//
//            // Get a list of EstimateIds from itemEstimates of which the ActId is 6 (complete) and 14 (cancelled)
//            ArrayList<String> jobItemEstimateIds = jobDataController.getJobItemEstimateIds(ActivityIdConstants.ESTIMATE_COMPLETE);
//            ArrayList<String> cancelledJobItemEstimateIds = jobDataController.getJobItemEstimateIds(ActivityIdConstants.ESTIMATE_CANCELLED);
//            jobItemEstimateIds.addAll(cancelledJobItemEstimateIds);
//
//            // Get a list of photo paths and filenames from job_item_estimate_photo where estimate_id in ^
//            ArrayList<String> jobItemEstimatePhotoIds = jobDataController.getJobItemEstimatePhotoIds(jobItemEstimateIds);
//            ArrayList<String> jobItemEstimatePhotos = jobDataController.getJobItemEstimatePhotos(jobItemEstimateIds);
//
//            // Get a list of estimate_works_ids from job_estimate_works of which the ActId is 21 (complete) and 20 (cancelled)
//            ArrayList<String> jobEstimateWorksIds = jobDataController.getJobEstimateWorksIds(ActivityIdConstants.EST_WORKS_COMPLETE);
//            ArrayList<String> cancelledJobEstimateWorksIds = jobDataController.getJobEstimateWorksIds(ActivityIdConstants.EST_WORKS_CANCELLED);
//            jobEstimateWorksIds.addAll(cancelledJobEstimateWorksIds);
//
//            // Get a list of photo paths and filenames from estimate_works_photo where estimate_works_id in ^
//            ArrayList<String> jobEstimateWorksPhotoIds = jobDataController.getJobEstimateWorksPhotoIds(jobEstimateWorksIds);
//            ArrayList<String> jobEstimateWorksPhotos = jobDataController.getJobEstimateWorksPhotos(jobItemEstimateIds);
//
//            // Delete all the photographs
//            PhotoUtil.cleanupDevice(jobItemEstimatePhotos);
//            PhotoUtil.cleanupDevice(jobEstimateWorksPhotos);
//            PhotoUtil.cleanupDevice(jobItemMeasurePhotos);
//
//            // Delet applicable records from the DB tables
//            jobDataController.jobSectionCleanup(jobIds);
//            jobDataController.jobItemMeasurePhotoCleanup(jobItemMeasurePhotoIds);
//            jobDataController.jobItemMeasureCleanup(jobItemMeasureIds);
//            jobDataController.jobCleanup(jobIds);
//            jobDataController.jobItemEstimatePhotoCleanup(jobItemEstimatePhotoIds);
//            jobDataController.jobItemEstimateCleanup(jobItemEstimateIds);
//            jobDataController.jobEstimateWorksPhotoCleanup(jobEstimateWorksPhotoIds);
//            jobDataController.jobEstimateWorksCleanup(jobEstimateWorksIds);
//
//        } catch (Exception ex) {
//            Toast.makeText(getApplicationContext(), R.string.device_cleanup_failed + ":\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void deleteSQLiteLookupTables() {
//        BaseDataSource baseDataSource = new BaseDataSource(getApplicationContext());
//        SQLiteDatabase sqLiteDatabase = baseDataSource.getDatabase();
//        if (sqLiteDatabase != null)
//            MySQLiteHelper.deleteLookupSQLiteDataTables(sqLiteDatabase);
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
////        enableBackNavigation();
//        getServiceVersion();
//    }

//    @SuppressLint("CheckResult") private void getServiceVersion() {
//        SettingsViewModel model = new SettingsViewModel();
//        RxUtils.schedule(model.getServiceVersion()).subscribe(new Consumer<String>() {
//            @Override public void accept(String s) throws Exception {
//                Log.d("x-v", s);
//            }
//        }, new Consumer<Throwable>() {
//            @Override public void accept(Throwable throwable) throws Exception {
//                Log.d("x-t", throwable.getMessage());
//            }
//        });
//    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        closeDataSources();
//    }

//    private void closeDataSources() {
//        jobItemEstimatePhotoDataSource.close();
//    }
//
//    private void openDataSources() {
//        jobItemEstimatePhotoDataSource.open();
//    }
//
//    private void createDataSources() {
//        jobItemEstimatePhotoDataSource = new JobItemEstimatePhotoDataSource(getApplicationContext());
//    }

//    private void initializeControllers() {
//        jobDataController = new JobDataController();
//        jobDataController.createDataSources(getApplicationContext());
//        jobDataController.open();
//
//        toDoListDataController = new ToDoListDataController();
//        toDoListDataController.CreateDataSources(getApplicationContext());
//        toDoListDataController.openDataSources();
//
//        projectController = new ProjectController(getApplicationContext());
//        roleController = new RoleController(getApplicationContext());
//
//        registerDataController = new RegisterDataController();
//        registerDataController.createDataSources(getApplicationContext());
//        registerDataController.openDataSources();
//
//        refreshWorkFlowItemsViewModel = new RefreshWorkFlowItemsViewModel(this, this, this);
//        refreshLookupDataViewModel = new RefreshLookupDataViewModel(this, this, this, this);
//
//        registrationInfoDataSource = new RegistrationInfoDataSource(getApplicationContext());
//    }
//
//    private void deletePhotosInDirectory() {
//        for (JobItemEstimatePhoto jobItemEstimatePhoto : jobItemEstimatePhotoDataSource.getAllJobItemEstimatePhotos()) {
//            try {
//                File file = new File(new URI(jobItemEstimatePhoto.getPhotoPath()));
//                file.delete();
//            } catch (NullPointerException | IllegalArgumentException | URISyntaxException e) {
//                Log.e(TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//        }
//    }

    public static Switch switch1;
    public static final String PREFS_NAME ="DarkeModeSwitch";
    boolean isChecked;

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onResume() {
//            if (){
//            }
            super.onResume();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final boolean isChecked = false;
            SwitchPreferenceCompat mytheme = findPreference(HOME);
            mytheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (newValue.equals(!isChecked)){
                        ((SettingsActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                    }else{

                        ((SettingsActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                    }

                    return true;
                }
            });




        }
    }




//    @Override
//    public void onSetErrorFlag(Boolean isError) {
//        errorOccurredDuringRegistration = isError;
//    }
//
//    @Override
//    public Boolean onGetErrorFlag() {
//        return errorOccurredDuringRegistration;
//    }

//    @Override
//    public void onInsertSectionsItems(ArrayList<String> activitySections) {
//        registerDataController.insertSectionsItems(activitySections);
//    }
//
//    @Override
//    public void onInsertContractsProjects(ArrayList<Contracts> offlinedata) {
//        registerDataController.insertContractsProjects(offlinedata);
//    }
//
//    @Override
//    public void onInsertWorkFlows(MobileWorkFlows workflows) {
//        registerDataController.insertWorkFlows(workflows);
//    }
//
//    @Override
//    public void onInsertMobileLookups2(ArrayList<MobileLookup2> mobileLookups) {
//        registerDataController.insertMobileLookups2(mobileLookups);
//    }
//
//    @Override
//    public void onInsertProjectItems(ArrayList<ItemDTO> items) {
//        registerDataController.insertProjectItems(items);
//    }
//
//    @Override
//    public void onInsertProjectSections(ArrayList<Section> sections) {
//        registerDataController.insertProjectSections(sections);
//    }
//
//    @Override
//    public void onInsertProjectVos(ArrayList<VoItemDTO> voItems) {
//        registerDataController.insertProjectVos(voItems);
//    }
//
//    @Override
//    public void onInsertEffectiveRoles(ArrayList<Role> effectiveRoles) {
//        registerDataController.insertEffectiveRoles(effectiveRoles);
//    }
//
//    @Override
//    public void insertUserRoles(ArrayList<Role> userRoles) {
//        registerDataController.insertUserRoles(userRoles);
//    }
//
//    @Override
//    public void onRequestPermissions() {
//        PermissionController.startPermissionRequests(this, getApplicationContext());
//    }
//
//    @Override
//    public void onNoConnection() {
//        Toast.makeText(getApplicationContext(), getString(R.string.no_connection_detected), Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onError() {
//        toast(R.string.error_getting_workflow_items);
//        dismissProgressDialog();
//    }
//
//    @Override
//    public void onSuccess() {
//        Log.i(TAG, getString(R.string.workflow_items_refreshed_successfully));
//        Toast.makeText(getApplicationContext(), R.string.workflow_items_refreshed_successfully, Toast.LENGTH_LONG).show();
//        dismissProgressDialog();
//    }

}