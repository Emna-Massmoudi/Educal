import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { TeacherPending } from './teacher-pending';

describe('TeacherPending', () => {
  let component: TeacherPending;
  let fixture: ComponentFixture<TeacherPending>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeacherPending],
      providers: [
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TeacherPending);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
