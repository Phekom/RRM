package za.co.xisystems.itis_rrm.ui.mainview.capture.usecases

import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models.PhotoType

class GeocodeCaptureUseCase(
    private val capturedPictureRepository: CapturedPictureRepository,
    private val jobCreationDataRepository: JobCreationDataRepository
) {
    fun execute(unallocatedPhotoId: String, estimateId: String, photoType: PhotoType): XIResult<String> {
        // collect captured picture from repo
        // check coordinates against sanral service
        // update routeMarker and StartKm
        // save to repo

        return XIResult.Error(NotImplementedError("Finish this please!"), "Seriously, finish this!")
    }
}
