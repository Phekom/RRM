package za.co.xisystems.itis_rrm.ui.mainview.estmeasure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import za.co.xisystems.itis_rrm.R;

public class MeasureFragment extends Fragment {
    public static final String JOB_MEASURE_EST_JOB_ID = "JOB_MEASURE_EST_JOB_ID";
    private static final String TAG = "";
//    protected ToDoListDataController toDoListDataController;
//    protected JobDescriptionAdapter jobDescriptionAdapter;
//    protected JobDataController jobDataController;
//    protected ArrayList<JobDTO> jobArrayList;

    protected ListView listView;
    protected ImageView noDataImageView;


    private MeasureViewModel sendViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sendViewModel =
                ViewModelProviders.of(this).get(MeasureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_estmeasure, container, false);
//        final TextView textView = root.findViewById(R.id.text_send);
        sendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });
        return root;
    }






















//
//    public void addEntitiesToJobArrayList(ToDoListDataController toDoListController,
//                                          ArrayList<EntitiesDTO> entitiesArrayList,
//                                          ArrayList<JobDTO> jobList) {
//        for (EntitiesDTO toDoListEntityDTOS : entitiesArrayList) {
//            PrimaryKeyValue primaryKeyValue = toDoListController.getPrimaryKeyValueForJobId(toDoListEntityDTOS.getJobId());
//            EntitiesDTO jobEntity = toDoListController.getEntityForTrackRouteId(primaryKeyValue.getTrackRouteId());
//            JobDTO job = new JobDTO();
//            job.setJobId(toDoListEntityDTOS.getJobId());
//            job.setDescr(jobEntity.getDescription());
//            ArrayList<EntitiesDTO> completed = toDoListDataController.getEntitiesForActivityId(ActivityIdConstants.MEASURE_COMPLETE);
//            if (!containsJob(job, jobList, completed))
//                jobList.add(job);
//        }
//
//    }
//
//
//    private void fetchJobRelatedInfo() {
//        ArrayList<JobItemEstimate> jobItemEstimateArrayList = jobDataController.getJobItemEstimatesForActivityId(ActivityIdConstants.ESTIMATE_MEASURE);
//        for (JobItemEstimate jobItemEstimate : jobItemEstimateArrayList) {
//            boolean recordsExist = jobDataController.checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobItemEstimate.getJobId(), jobItemEstimate.getEstimateId());
//            if (!recordsExist) {
//                String jobId = jobItemEstimate.getJobId();
//                JobDTO job = jobDataController.getJobFromJobId(jobId);
//                if (job == null) {
//                    Log.d("x-error", getClass().getSimpleName() + "::fetchJobRelatedInfo: job[" + jobId + "] is null");
//                } else {
//                    Log.d("x-m", getClass().getSimpleName() + "::fetchJobRelatedInfo: job[" + jobId + "] is ok");
//                    if (job.getActId() == ActivityIdConstants.JOB_FAILED) continue;
//                    if (!containsJob(job, jobArrayList, null))
//                        jobArrayList.add(job);
//                }
//            }
//        }
//        addEntitiesToJobArrayList(toDoListDataController,
//                toDoListDataController.getEntitiesForActivityId(ActivityIdConstants.ESTIMATE_MEASURE),
//                jobArrayList);
//    }
//
//    private static boolean containsJob(JobDTO jobToBeAdded, ArrayList<JobDTO> jobArrayList, ArrayList<EntitiesDTO> completed) {
//        for (JobDTO job : jobArrayList) {
//            if (job.getJobId().equalsIgnoreCase(jobToBeAdded.getJobId()))
//                return true;
//            //  Check if we have the JobDTO Id that is already completed.
//            if (completed != null)
//                for (EntitiesDTO check : completed) {
//                    if (check.getJobId().equalsIgnoreCase(jobToBeAdded.getJobId())) {
//                        return true;
//                    }
//                }
//        }
//        return false;
//    }
//
//
//











}