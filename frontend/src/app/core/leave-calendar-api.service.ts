import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateLeaveRequest,
  LeaveRequest,
  OnCallWeek,
  TeamMember,
  UpdateLeaveStatusRequest,
} from './api.models';

@Injectable({
  providedIn: 'root',
})
export class LeaveCalendarApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  getTeamMembers(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.apiBaseUrl}/team-members`);
  }

  getLeaveRequests(teamMember?: TeamMember | 'ALL'): Observable<LeaveRequest[]> {
    let params = new HttpParams();

    if (teamMember && teamMember !== 'ALL') {
      params = params.set('teamMember', teamMember);
    }

    return this.http.get<LeaveRequest[]>(`${this.apiBaseUrl}/leave-requests`, {
      params,
    });
  }

  createLeaveRequest(request: CreateLeaveRequest): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(`${this.apiBaseUrl}/leave-requests`, request);
  }

  updateLeaveStatus(id: number, request: UpdateLeaveStatusRequest): Observable<LeaveRequest> {
    return this.http.patch<LeaveRequest>(`${this.apiBaseUrl}/leave-requests/${id}/status`, request);
  }

  getOnCallSchedule(from: string, weeks: number): Observable<OnCallWeek[]> {
    const params = new HttpParams().set('from', from).set('weeks', weeks);

    return this.http.get<OnCallWeek[]>(`${this.apiBaseUrl}/on-call`, {
      params,
    });
  }
}
