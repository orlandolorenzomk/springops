import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OsInfoComponent } from './os-info.component';

describe('OsInfoComponent', () => {
  let component: OsInfoComponent;
  let fixture: ComponentFixture<OsInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OsInfoComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OsInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
