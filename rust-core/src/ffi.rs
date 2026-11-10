use std::ffi::{c_char, CStr, CString};
use std::ptr;

use crate::ObfuscationContext;

unsafe fn read_string(value: *const c_char) -> Result<String, ()> {
    if value.is_null() {
        return Err(());
    }
    CStr::from_ptr(value)
        .to_str()
        .map(str::to_string)
        .map_err(|_| ())
}

fn c_string(value: &str) -> *mut c_char {
    CString::new(value).unwrap_or_default().into_raw()
}

/* Creates an opaque context for Java/JNA and C#/P/Invoke callers. */
#[no_mangle]
pub unsafe extern "C" fn glsl_context_new(
    excluded_csv: *const c_char,
    separate: u8,
    seed: i64,
) -> *mut ObfuscationContext {
    let excluded = read_string(excluded_csv).unwrap_or_default();
    let values = excluded
        .split('\n')
        .filter(|value| !value.is_empty())
        .map(str::to_string)
        .collect::<Vec<_>>();
    Box::into_raw(Box::new(ObfuscationContext::new(
        &values,
        separate != 0,
        (seed >= 0).then_some(seed as u64),
    )))
}

/* Adds declarations from one source file to the shared context. */
#[no_mangle]
pub unsafe extern "C" fn glsl_context_collect(
    context: *mut ObfuscationContext,
    source: *const c_char,
) -> u8 {
    if context.is_null() {
        return 0;
    }
    match read_string(source) {
        Ok(source) => (*context).collect(&source),
        Err(()) => return 0,
    }
    1
}

/* Transforms source and returns an allocated UTF-8 string owned by the caller. */
#[no_mangle]
pub unsafe extern "C" fn glsl_context_transform(
    context: *const ObfuscationContext,
    source: *const c_char,
    minify: u8,
) -> *mut c_char {
    if context.is_null() {
        return ptr::null_mut();
    }
    match read_string(source) {
        Ok(source) => c_string(&(*context).transform(&source, minify != 0)),
        Err(()) => ptr::null_mut(),
    }
}

/* Releases a context returned by glsl_context_new. */
#[no_mangle]
pub unsafe extern "C" fn glsl_context_free(context: *mut ObfuscationContext) {
    if !context.is_null() {
        drop(Box::from_raw(context));
    }
}

/* Releases a string returned by glsl_context_transform. */
#[no_mangle]
pub unsafe extern "C" fn glsl_string_free(value: *mut c_char) {
    if !value.is_null() {
        drop(CString::from_raw(value));
    }
}
