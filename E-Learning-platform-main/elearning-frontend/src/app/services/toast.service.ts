import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface ToastMessage {
  id: number;
  type: ToastType;
  title: string;
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new Subject<ToastMessage>();
  toasts$ = this.toastsSubject.asObservable();
  private currentId = 0;

  show(type: ToastType, title: string, message: string, duration = 4000): void {
    this.toastsSubject.next({
      id: this.currentId++,
      type,
      title,
      message,
      duration
    });
  }

  success(title: string, message: string, duration = 4000): void {
    this.show('success', title, message, duration);
  }

  error(title: string, message: string, duration = 5000): void {
    this.show('error', title, message, duration);
  }

  info(title: string, message: string, duration = 4000): void {
    this.show('info', title, message, duration);
  }

  warning(title: string, message: string, duration = 4000): void {
    this.show('warning', title, message, duration);
  }
}
