import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { FormateurService } from '../../services/formateur';
import { TeacherApplication } from './teacher-application';

describe('TeacherApplication', () => {
  let component: TeacherApplication;
  let fixture: ComponentFixture<TeacherApplication>;

  beforeEach(async () => {
    TestBed.overrideComponent(TeacherApplication, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      declarations: [TeacherApplication],
      providers: [
        {
          provide: FormateurService,
          useValue: {},
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TeacherApplication);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
