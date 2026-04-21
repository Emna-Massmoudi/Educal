import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { CoursService } from '../../services/cours';
import { QuizService } from '../../services/quiz';
import { TeacherQuiz } from './teacher-quiz';

describe('TeacherQuiz', () => {
  let component: TeacherQuiz;
  let fixture: ComponentFixture<TeacherQuiz>;

  beforeEach(async () => {
    TestBed.overrideComponent(TeacherQuiz, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [TeacherQuiz],
      providers: [
        {
          provide: CoursService,
          useValue: {
            getCoursByFormateur: () => of([]),
          },
        },
        {
          provide: QuizService,
          useValue: {},
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TeacherQuiz);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
