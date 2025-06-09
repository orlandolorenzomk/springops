import { TestBed } from '@angular/core/testing';

import { SystemVersionService } from './system-version.service';

describe('SystemVersionService', () => {
  let service: SystemVersionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SystemVersionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
