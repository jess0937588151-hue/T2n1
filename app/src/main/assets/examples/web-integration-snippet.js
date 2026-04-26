(function () {
  function printOrder(order) {
    if (!window.SunmiPrinterBridge) return;
    var lines = [];
    lines.push('訂單編號：' + (order.orderNo || ''));
    lines.push('客戶：' + (order.customerName || ''));
    lines.push('------------------------------');
    (order.items || []).forEach(function (item) {
      lines.push(item.name + ' x ' + item.qty + '  $' + item.price);
    });
    lines.push('------------------------------');
    lines.push('總計：$' + (order.total || 0));
    if (order.note) lines.push('備註：' + order.note);

    window.SunmiPrinterBridge.printReceipt('顧客單', lines.join('\n'));
  }

  window.demoSunmiPrintOrder = printOrder;
})();
