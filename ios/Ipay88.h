#import "Ipay.h"
#import "IpayPayment.h"
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>

@interface Ipay88 : RCTEventEmitter <RCTBridgeModule>

@property UIView *paymentView;
@property Ipay *paymentsdk;
@property IpayPayment *payment;
@property UIViewController *popupScreen;

@end
