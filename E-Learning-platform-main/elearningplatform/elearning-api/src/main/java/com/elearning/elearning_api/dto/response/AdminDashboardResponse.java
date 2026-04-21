package com.elearning.elearning_api.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class AdminDashboardResponse {
    private AdminDashboardOverviewResponse overview;
    private List<CoursResponse> recentCourses;
    private List<CoursResponse> topCourses;
    private List<FormateurResponse> pendingFormateurs;
    private List<PaiementCoursResponse> pendingPayments;
}
