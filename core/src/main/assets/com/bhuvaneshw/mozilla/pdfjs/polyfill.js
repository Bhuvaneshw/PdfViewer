if (!URL.parse) {
   URL.parse = function (urlStr, base) {
      try {
        if (base) {
          return new URL(urlStr, base);
        }
        return new URL(urlStr);
      } catch (e) {
        return null;
      }
    };
}
