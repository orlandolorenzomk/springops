import { TestBed } from '@angular/core/testing';

import { ApplicationStatsService } from './application-stats.service';

describe('ApplicationStatsService', () => {
  let service: ApplicationStatsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApplicationStatsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
