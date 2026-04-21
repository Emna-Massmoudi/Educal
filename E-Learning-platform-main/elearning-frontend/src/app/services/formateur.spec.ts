import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { FormateurService } from './formateur';

describe('FormateurService', () => {
  let service: FormateurService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
    service = TestBed.inject(FormateurService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
