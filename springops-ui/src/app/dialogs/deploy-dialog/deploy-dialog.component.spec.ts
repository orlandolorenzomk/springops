import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeployDialogComponent } from './deploy-dialog.component';

describe('DeployDialogComponent', () => {
  let component: DeployDialogComponent;
  let fixture: ComponentFixture<DeployDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DeployDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeployDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
