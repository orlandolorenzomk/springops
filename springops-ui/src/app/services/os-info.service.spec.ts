import { TestBed } from '@angular/core/testing';

import { OsInfoService } from './os-info.service';

describe('OsInfoService', () => {
  let service: OsInfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OsInfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
